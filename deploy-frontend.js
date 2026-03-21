const { Client } = require('ssh2');
const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

// --- Configuration ---
const HOST = '111.228.5.192';
const PORT = 22;
const USER = 'root';
const PRIVATE_KEY_PATH = path.join(process.env.USERPROFILE, '.ssh', 'id_ed25519');
const LOCAL_DIST_DIR = path.join(__dirname, 'frontend', 'dist');
const REMOTE_DIST_DIR = '/opt/zwj/resumeDemo/frontend/dist';
const REMOTE_BACKUP_DIR = `/opt/zwj/resumeDemo/frontend/dist_backup_${Date.now()}`;

console.log('🚀 Starting Frontend Deployment...');

// 1. Build the Vue application
try {
    console.log('📦 Building frontend (npm run build)...');
    execSync('npm run build', { cwd: path.join(__dirname, 'frontend'), stdio: 'inherit' });
    console.log('✅ Build successful.\n');
} catch (error) {
    console.error('❌ Build failed:', error.message);
    process.exit(1);
}

// 2. Connect via SSH and upload using ssh2 module (bypasses Windows scp quirks)
console.log(`🔌 Connecting via ssh2 to server ${HOST}...`);
const conn = new Client();

conn.on('ready', () => {
    console.log('✅ SSH Connected.');

    // Backup remote directory and create new one
    const initCmd = `mv ${REMOTE_DIST_DIR} ${REMOTE_BACKUP_DIR} 2>/dev/null || true && mkdir -p ${REMOTE_DIST_DIR}`;
    console.log(`📡 Backing up remote directory: ${REMOTE_DIST_DIR} -> ${REMOTE_BACKUP_DIR}`);

    conn.exec(initCmd, (err, stream) => {
        if (err) throw err;
        stream.on('data', (data) => {
            console.log('STDOUT: ' + data);
        }).stderr.on('data', (data) => {
            console.log('STDERR: ' + data);
        });
        stream.on('close', (code, signal) => {
            console.log('✅ Remote directory prepared. Starting SFTP recursive upload...');

            conn.sftp((err, sftp) => {
                if (err) throw err;

                // Recursively upload directory using native SFTP
                const uploadDir = (localPath, remotePath) => {
                    return new Promise((resolve, reject) => {
                        const items = fs.readdirSync(localPath);
                        let pending = items.length;

                        if (!pending) return resolve();

                        sftp.mkdir(remotePath, (err) => {
                            // Ignore mkdir errors if it exists
                            items.forEach((item) => {
                                const lp = path.join(localPath, item);
                                const rp = `${remotePath}/${item}`;

                                if (fs.statSync(lp).isDirectory()) {
                                    uploadDir(lp, rp).then(() => {
                                        if (--pending === 0) resolve();
                                    }).catch(reject);
                                } else {
                                    sftp.fastPut(lp, rp, (err) => {
                                        if (err) return reject(err);
                                        console.log(`   ⬆️ Uploaded: ${rp}`);
                                        if (--pending === 0) resolve();
                                    });
                                }
                            });
                        });
                    });
                };

                uploadDir(LOCAL_DIST_DIR, REMOTE_DIST_DIR)
                    .then(() => {
                        console.log('\n🎉 Frontend Deployment Completed Successfully!');
                        conn.end();
                    })
                    .catch((err) => {
                        console.error('\n❌ SFTP Upload Error:', err);
                        conn.end();
                    });
            });

        });
    });
}).on('error', (err) => {
    console.error('❌ SSH Connection Error:', err.message);
}).connect({
    host: HOST,
    port: PORT,
    username: USER,
    privateKey: fs.readFileSync(PRIVATE_KEY_PATH)
});
