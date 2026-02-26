#!/usr/bin/env node
const mysql = require('mysql2/promise');

const connection = mysql.createPool({
  host: '111.228.5.192',
  user: 'root',
  password: '123456',
  database: 'resume_demo',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

async function listResources() {
  return {
    resources: [
      {
        uri: 'mysql://resume_demo/students',
        name: 'Students Table',
        mimeType: 'application/sql'
      },
      {
        uri: 'mysql://resume_demo/attendance',
        name: 'Attendance Table',
        mimeType: 'application/sql'
      },
      {
        uri: 'mysql://resume_demo/exam_score',
        name: 'Exam Score Table',
        mimeType: 'application/sql'
      },
      {
        uri: 'mysql://resume_demo/class_diary',
        name: 'Class Diary Table',
        mimeType: 'application/sql'
      }
    ]
  };
}

async function executeQuery(sql) {
  try {
    const [results] = await connection.execute(sql);
    return { success: true, data: results };
  } catch (error) {
    return { success: false, error: error.message };
  }
}

// MCP协议处理
const readline = require('readline');
const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
  terminal: false
});

rl.on('line', async (line) => {
  try {
    const request = JSON.parse(line);
    let response;

    switch (request.method) {
      case 'initialize':
        response = {
          jsonrpc: '2.0',
          id: request.id,
          result: {
            protocolVersion: '2024-11-05',
            capabilities: {
              resources: { listChanged: false },
              tools: {}
            },
            serverInfo: {
              name: 'mysql-mcp-server',
              version: '1.0.0'
            }
          }
        };
        break;

      case 'resources/list':
        response = {
          jsonrpc: '2.0',
          id: request.id,
          result: await listResources()
        };
        break;

      case 'resources/read':
        const sql = request.params.uri.replace('mysql://resume_demo/', '');
        const result = await executeQuery(`SELECT * FROM ${sql} LIMIT 10`);
        response = {
          jsonrpc: '2.0',
          id: request.id,
          result: {
            contents: [{
              uri: request.params.uri,
              mimeType: 'application/json',
              text: JSON.stringify(result, null, 2)
            }]
          }
        };
        break;

      case 'tools/call':
        if (request.params.name === 'execute_sql') {
          const sqlResult = await executeQuery(request.params.arguments.sql);
          response = {
            jsonrpc: '2.0',
            id: request.id,
            result: {
              content: [{
                type: 'text',
                text: JSON.stringify(sqlResult, null, 2)
              }]
            }
          };
        }
        break;

      default:
        response = {
          jsonrpc: '2.0',
          id: request.id,
          error: {
            code: -32601,
            message: 'Method not found'
          }
        };
    }

    console.log(JSON.stringify(response));
  } catch (error) {
    console.error('Error:', error);
  }
});

console.error('MySQL MCP Server started');
