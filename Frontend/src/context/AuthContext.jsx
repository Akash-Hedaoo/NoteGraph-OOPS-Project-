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
  const [loading, setLoading] = useState(true); // Usually to wait for async profile fetch, here we can set false

  useEffect(() => {
    // If we have a token, we could fetch user profile here if we didn't save it.
    // For now we assume if token and user exist in localstorage, we're logged in.
    setLoading(false);
  }, []);

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
  };

  const value = {
    user,
    token,
    workspaceId,
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
