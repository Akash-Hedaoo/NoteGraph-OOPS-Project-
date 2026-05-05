const graphController = require('./src/controllers/graph.controller');
const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();
const { stringify } = require('uuid');

async function test() {
    const ws = await prisma.workspaces.findFirst({ where: { name: 'professional workspace' } });
    if (!ws) return console.log('no ws');
    const wsIdStr = stringify(ws.id);
    
    console.log('Testing GraphController with WS:', wsIdStr);
    
    const req = { params: { workspaceId: wsIdStr } };
    const res = {
        json: (data) => console.log('JSON Output:', JSON.stringify(data, null, 2).substring(0, 500)),
        status: (code) => ({ json: (err) => console.log('Status', code, err) })
    };
    
    await graphController.getGraphData(req, res);
    await prisma.$disconnect();
}
test();
