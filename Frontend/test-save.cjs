const axios = require('axios');

async function testSave() {
  try {
    // 1. Register/Login a dummy user to get token
    const authRes = await axios.post('http://localhost:8080/api/auth/register', {
      email: 'test' + Date.now() + '@test.com',
      password: 'password123',
      name: 'Testy McTest'
    }).catch(async (e) => {
      // maybe login if register fails
      return axios.post('http://localhost:8080/api/auth/login', {
        email: 'test@test.com', password: 'password123'
      });
    });

    const { token, user, workspaceId } = authRes.data;

    // 2. Try to create a note
    const payload = {
      title: 'Untitled Note',
      content: 'This is test content',
      workspace: { id: workspaceId },
      owner: { id: user.id }
    };

    console.log("Sending payload:", JSON.stringify(payload));
    const noteRes = await axios.post('http://localhost:8080/api/notes', payload, {
      headers: { Authorization: `Bearer ${token}` }
    });
    
    console.log("Success! Note:", noteRes.data);
  } catch (error) {
    console.error("FAILED.");
    if (error.response) {
      console.error("Status:", error.response.status);
      console.error("Body:", JSON.stringify(error.response.data, null, 2));
    } else {
      console.error(error.message);
    }
  }
}

testSave();
