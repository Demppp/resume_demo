const { Client } = require('ssh2');
const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

// --- Configuration ---
const HOST = '111.228.5.192';
const PORT = 22;
const USER = 'root';
const PRIVATE_KEY_PATH = path.join(process.env.USERPROFILE, '.ssh', 'id_ed25519');
const BACKEND_DIR = path.join(__dirname, 'backend');
const LOCAL_JAR_PATH = path.join(BACKEND_DIR, 'target', 'class-management-system-1.0.0.jar');
const REMOTE_DIR = '/opt/zwj/resumeDemo';
const REMOTE_JAR_PATH = `${REMOTE_DIR}/class-management-system-1.0.0.jar`;

console.log('🚀 Starting Backend Deployment...');

// 1. Build the Spring Boot application
try {
    console.log('📦 Building backend (mvn clean package -DskipTests)...');
    execSync('mvn.cmd clean package -DskipTests', { cwd: BACKEND_DIR, stdio: 'inherit' });
    console.log('✅ Build successful.\n');
} catch (error) {
    console.error('❌ Build failed:', error.message);
    process.exit(1);
}

// 2. Connect via SSH to stop, upload, and start
console.log(`🔌 Connecting via ssh2 to server ${HOST}...`);
const conn = new Client();

conn.on('ready', () => {
    console.log('✅ SSH Connected.');

    console.log(`🔌 Stopping remote application on ${HOST}...`);
    conn.exec(`cd ${REMOTE_DIR} && ./stop.sh`, (err, stream) => {
        if (err) throw err;
        stream.on('close', (code, signal) => {
            console.log('✅ Remote application stopped.');

            console.log(`\n⬆️ Uploading new JAR file...`);
            conn.sftp((err, sftp) => {
                if (err) throw err;

                sftp.fastPut(LOCAL_JAR_PATH, REMOTE_JAR_PATH, (err) => {
                    if (err) throw err;
                    console.log('✅ JAR uploaded successfully.');

                    console.log(`\n♻️ Starting remote application...`);
                    const startCmd = `cd ${REMOTE_DIR} && ./start.sh && echo '--- Tailing logs for 10 seconds ---' && timeout 10 tail -f nohup.out || true`;

                    conn.exec(startCmd, (err, startStream) => {
                        if (err) throw err;
                        startStream.on('close', (code, signal) => {
                            console.log('\n🎉 Backend Deployment Completed Successfully!');
                            conn.end();
                        }).on('data', (data) => {
                            process.stdout.write(data);
                        }).stderr.on('data', (data) => {
                            process.stderr.write(data);
                        });
                    });
                });
            });

        }).on('data', (data) => {
            process.stdout.write(data);
        }).stderr.on('data', (data) => {
            process.stderr.write(data);
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
