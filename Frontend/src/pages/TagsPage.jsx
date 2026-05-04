import React, { useState, useEffect } from 'react';
import { Search, Plus, MoreHorizontal, Edit2, Trash2, X, Tag as TagIcon } from 'lucide-react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import ConfirmModal from '../components/ConfirmModal';
import './TagsPage.css';

const COLORS = ['blue', 'purple', 'green', 'orange', 'gray', 'red'];

const TagsPage = () => {
  const { workspaceId } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);
  
  const [tags, setTags] = useState([]);
  const [loading, setLoading] = useState(true);

  const [newTagName, setNewTagName] = useState('');
  const [newTagColor, setNewTagColor] = useState('blue');

  // Edit Tag State
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingTag, setEditingTag] = useState(null);
  const [editTagName, setEditTagName] = useState('');
  const [editTagColor, setEditTagColor] = useState('blue');

  const fetchTags = async () => {
    if (!workspaceId) return;
    try {
      const response = await api.get(`/tags/workspace/${workspaceId}`);
      // Sort alphabetically or however desired
      setTags(response.data);
    } catch (error) {
      console.error("Failed to load tags", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTags();
  }, [workspaceId]);

  const handleCreateTag = async (e) => {
    e.preventDefault();
    if (!newTagName.trim() || !workspaceId) return;
    
    try {
      await api.post('/tags', {
        name: newTagName.trim(),
        color: newTagColor,
        workspace: { id: workspaceId }
      });
      setNewTagName('');
      setIsCreateModalOpen(false);
      fetchTags(); // Refresh list
    } catch (error) {
      console.error("Failed to create tag", error);
    }
  };

  const openEditModal = (tag) => {
    setEditingTag(tag);
    setEditTagName(tag.name);
    setEditTagColor(tag.color || 'blue');
    setIsEditModalOpen(true);
  };

  const handleEditTag = async (e) => {
    e.preventDefault();
    if (!editTagName.trim() || !editingTag) return;
    
    try {
      await api.put(`/tags/${editingTag.id}`, {
        ...editingTag,
        name: editTagName.trim(),
        color: editTagColor
      });
      setIsEditModalOpen(false);
      setEditingTag(null);
      fetchTags();
    } catch (error) {
      console.error("Failed to edit tag", error);
    }
  };

  const handleDeleteTag = (id) => {
    setConfirmAction({
      title: 'Delete Tag',
      message: 'Are you sure you want to delete this tag? Connections to notes will be lost.',
      onConfirm: async () => {
        setConfirmAction(null);
        try {
          await api.delete(`/tags/${id}`);
          fetchTags();
        } catch (error) {
          console.error("Failed to delete tag", error);
        }
      }
    });
  };

  const filteredTags = tags.filter(tag => 
    tag.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="tags-page-container">
      <header className="page-header-row">
        <div className="header-actions-flex">
          <div className="title-group">
            <h1 className="page-title">Tags Management</h1>
            <p className="page-subtitle">Organize and manage your note tags across all workspaces.</p>
          </div>
          <button 
            className="btn-primary" 
            onClick={() => setIsCreateModalOpen(true)}
          >
            <Plus size={18} /> Create New Tag
          </button>
        </div>
      </header>

      <div className="tags-content card">
        {/* Toolbar */}
        <div className="tags-toolbar">
          <div className="search-container small">
            <Search className="search-icon" size={16} />
            <input 
              type="text" 
              placeholder="Search tags..." 
              className="search-input"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          <div className="toolbar-stats">
            <span className="text-secondary">{tags.length} Total Tags</span>
          </div>
        </div>

        {/* Tags Grid */}
        <div className="tags-grid">
          {loading ? (
            <div style={{color: 'var(--text-secondary)'}}>Loading tags...</div>
          ) : filteredTags.length === 0 ? (
            <div style={{color: 'var(--text-secondary)'}}>No tags found. Create one to get started!</div>
          ) : (
            filteredTags.map((tag, index) => {
              const color = tag.color || COLORS[index % COLORS.length];
              return (
              <div key={tag.id} className="tag-management-card">
                <div className="tag-card-header">
                  <span className={`tag-pill badge-outline border-${color} text-${color}`}>
                    <span className={`dot bg-${color}`}></span>
                    #{tag.name}
                  </span>
                  <button className="icon-btn-small"><MoreHorizontal size={16} /></button>
                </div>
                
                <div className="tag-card-stats">
                  <div className="stat-row">
                    <span className="stat-label">System Data</span>
                    <span className="stat-val">Generated dynamic usages via Graph component</span>
                  </div>
                </div>

                <div className="tag-card-actions">
                   <button className="action-btn text-secondary hover-blue" onClick={() => openEditModal(tag)}><Edit2 size={14} /> Edit</button>
                   <button className="action-btn text-secondary hover-red" onClick={() => handleDeleteTag(tag.id)}><Trash2 size={14} /> Delete</button>
                </div>
              </div>
            )})
          )}
        </div>
      </div>

      {/* Create Modal */}
      {isCreateModalOpen && (
        <div className="modal-backdrop" onClick={() => setIsCreateModalOpen(false)}>
          <div className="create-tag-modal card" onClick={e => e.stopPropagation()}>
            <div className="modal-header-simple">
              <h2 className="modal-title">Create New Tag</h2>
              <button className="icon-btn-small" onClick={() => setIsCreateModalOpen(false)}><X size={20} /></button>
            </div>
            
            <form onSubmit={handleCreateTag} className="modal-body form-body">
              <div className="form-group">
                <label>Tag Name</label>
                <div className="input-with-prefix">
                  <span className="prefix">#</span>
                  <input 
                    type="text" 
                    className="input-field" 
                    placeholder="e.g. backend, meeting, urgent"
                    value={newTagName}
                    onChange={(e) => setNewTagName(e.target.value)}
                    autoFocus
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Color Badge</label>
                <div className="color-picker-row">
                  {['blue', 'purple', 'green', 'orange', 'gray'].map(color => (
                    <button 
                      key={color}
                      type="button"
                      className={`color-swatch-btn ${newTagColor === color ? 'selected' : ''}`}
                      onClick={() => setNewTagColor(color)}
                    >
                      <div className={`color-swatch bg-${color}`}></div>
                    </button>
                  ))}
                </div>
              </div>

              <div className="modal-footer-actions no-pad">
                <button type="button" className="btn-outline" onClick={() => setIsCreateModalOpen(false)}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={!newTagName.trim()}>Create Tag</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit Modal */}
      {isEditModalOpen && (
        <div className="modal-backdrop" onClick={() => setIsEditModalOpen(false)}>
          <div className="create-tag-modal card" onClick={e => e.stopPropagation()}>
            <div className="modal-header-simple">
              <h2 className="modal-title">Edit Tag</h2>
              <button className="icon-btn-small" onClick={() => setIsEditModalOpen(false)}><X size={20} /></button>
            </div>
            
            <form onSubmit={handleEditTag} className="modal-body form-body">
              <div className="form-group">
                <label>Tag Name</label>
                <div className="input-with-prefix">
                  <span className="prefix">#</span>
                  <input 
                    type="text" 
                    className="input-field" 
                    value={editTagName}
                    onChange={(e) => setEditTagName(e.target.value)}
                    autoFocus
                  />
                </div>
              </div>

              <div className="form-group">
                <label>Color Badge</label>
                <div className="color-picker-row">
                  {['blue', 'purple', 'green', 'orange', 'gray', 'red'].map(color => (
                    <button 
                      key={color}
                      type="button"
                      className={`color-swatch-btn ${editTagColor === color ? 'selected' : ''}`}
                      onClick={() => setEditTagColor(color)}
                    >
                      <div className={`color-swatch bg-${color}`}></div>
                    </button>
                  ))}
                </div>
              </div>

              <div className="modal-footer-actions no-pad">
                <button type="button" className="btn-outline" onClick={() => setIsEditModalOpen(false)}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={!editTagName.trim()}>Save Changes</button>
              </div>
            </form>
          </div>
        </div>
      )}

      <ConfirmModal
        isOpen={!!confirmAction}
        title={confirmAction?.title}
        message={confirmAction?.message}
        onConfirm={confirmAction?.onConfirm}
        onCancel={() => setConfirmAction(null)}
      />
    </div>
  );
};

export default TagsPage;
