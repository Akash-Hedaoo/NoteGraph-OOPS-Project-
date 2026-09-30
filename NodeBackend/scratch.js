const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();
const { stringify } = require('uuid');

async function run() {
  const ws = await prisma.workspaces.findFirst({ where: { name: 'professional workspace' } });
  if (!ws) {
      console.log('Workspace not found');
      return;
  }
  
  const tags = await prisma.tags.findMany({ where: { workspace_id: ws.id } });
  console.log('Tags count:', tags.length);
  
  const notes = await prisma.notes.findMany({ where: { workspace_id: ws.id } });
  console.log('Notes count:', notes.length);

  await prisma.$disconnect();
}
run();
