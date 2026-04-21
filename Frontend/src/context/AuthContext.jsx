import React, { createContext, useState, useEffect, useContext } from 'react';
import api from '../services/api';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    // Check for saved user in localStorage
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const [workspaceId, setWorkspaceId] = useState(() => localStorage.getItem('workspaceId')); // User's default workspace
  const [workspaces, setWorkspaces] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchWorkspaces = async () => {
    try {
      if (localStorage.getItem('token')) {
        const res = await api.get('/workspaces');
        setWorkspaces(res.data);
      }
    } catch (e) {
      console.error("Failed to fetch workspaces", e);
    }
  };

  useEffect(() => {
    const init = async () => {
       if (token) {
           await fetchWorkspaces();
       }
       setLoading(false);
    };
    init();
  }, [token]);

  const login = async (email, password) => {
    try {
      const response = await api.post('/auth/login', { email, password });
      const { token, user, workspaceId: defaultWorkspaceId } = response.data;
      
      // Save data
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      localStorage.setItem('workspaceId', defaultWorkspaceId);
      
      setUser(user);
      setToken(token);
      setWorkspaceId(defaultWorkspaceId);
      
      return true;
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  };

  const register = async (email, password, name) => {
    try {
      const response = await api.post('/auth/register', { email, password, name });
      const { token, user, workspaceId: defaultWorkspaceId } = response.data;
      
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      localStorage.setItem('workspaceId', defaultWorkspaceId);
      
      setUser(user);
      setToken(token);
      setWorkspaceId(defaultWorkspaceId);
      
      return true;
    } catch (error) {
      console.error('Registration failed:', error);
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('workspaceId');
    setUser(null);
    setToken(null);
    setWorkspaceId(null);
    setWorkspaces([]);
  };

  const setAndSaveWorkspaceId = (id) => {
    localStorage.setItem('workspaceId', id);
    setWorkspaceId(id);
  };

  const value = {
    user,
    token,
    workspaceId,
    setWorkspaceId: setAndSaveWorkspaceId,
    workspaces,
    fetchWorkspaces,
    login,
    register,
    logout,
    isAuthenticated: !!token,
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
