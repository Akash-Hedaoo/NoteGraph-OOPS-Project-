import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import DashboardLayout from './components/layout/DashboardLayout';
import DashboardOverview from './pages/DashboardOverview';
import NoteHierarchy from './pages/NoteHierarchy';
import Editor from './pages/Editor';
import TagsPage from './pages/TagsPage';
import FavoritesPage from './pages/FavoritesPage';
import ProtectedRoute from './components/shared/ProtectedRoute';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<Login />} />
        
        {/* Authenticated Routes Wrapped in Layout */}
        <Route element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }>
          <Route path="/dashboard" element={<DashboardOverview />} />
          <Route path="/hierarchy" element={<NoteHierarchy />} />
          <Route path="/editor" element={<Editor />} />
          <Route path="/editor/:id" element={<Editor />} />
          <Route path="/tags" element={<TagsPage />} />
          <Route path="/favorites" element={<FavoritesPage />} />
          
          {/* Map mockup Sidebar Links to the existing views so they "work" */}
          <Route path="/folders" element={<NoteHierarchy />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
