import React, { useState, useEffect } from 'react';
import { Search, Star, FileText, MoreHorizontal } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import './FavoritesPage.css';

const FavoritesPage = () => {
  const navigate = useNavigate();
  const { workspaceId } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  
  const [favorites, setFavorites] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchFavorites = async () => {
      if (!workspaceId) return;
      try {
        const res = await api.get(`/notes/workspace/${workspaceId}`);
        const favNotes = res.data.filter(n => n.favorite === true);
        setFavorites(favNotes);
      } catch (error) {
        console.error("Failed to load favorites", error);
      } finally {
        setLoading(false);
      }
    };
    fetchFavorites();
  }, [workspaceId]);

  const toggleFavorite = async (id) => {
    try {
      await api.put(`/notes/${id}/favorite`);
      setFavorites(favorites.filter(fav => fav.id !== id));
    } catch (err) {
      console.error("Failed to untoggle favorite", err);
    }
  };

  const stripHtml = (html) => {
    if (!html) return '';
    let tmp = document.createElement("DIV");
    tmp.innerHTML = html;
    return tmp.textContent || tmp.innerText || "";
  };

  const filteredFavs = favorites.filter(fav => 
    (fav.title || '').toLowerCase().includes(searchQuery.toLowerCase()) || 
    (fav.tags && fav.tags.some(t => t.name.toLowerCase().includes(searchQuery.toLowerCase())))
  );

  return (
    <div className="favorites-page-container">
      <header className="page-header-row">
        <div className="header-actions-flex">
          <div className="title-group">
            <h1 className="page-title flex-title">
              <Star fill="#F59E0B" color="#F59E0B" size={28} className="title-icon" /> 
              Favorites
            </h1>
            <p className="page-subtitle">Your most important notes, one click away.</p>
          </div>
          
          <div className="search-container small bg-white">
            <Search className="search-icon" size={16} />
            <input 
              type="text" 
              placeholder="Filter favorites..." 
              className="search-input"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
        </div>
      </header>

      {loading ? (
        <div className="loading-state" style={{padding: '2rem', color: 'var(--text-secondary)'}}>Loading favorites...</div>
      ) : filteredFavs.length === 0 ? (
        <div className="empty-state card">
          <Star size={48} className="text-secondary opacity-50 mb-4" />
          <h3>No Favorites Found</h3>
          <p className="text-secondary">You haven't added any notes to your favorites yet. Click the star icon on any note to add it here.</p>
        </div>
      ) : (
        <div className="favorites-grid">
          {filteredFavs.map(fav => (
            <div key={fav.id} className="note-card card fav-card" onClick={() => navigate(`/editor/${fav.id}`)}>
              <div className="fav-card-header">
                {fav.tags && fav.tags.length > 0 ? (
                  <span className={`tag-badge bg-blue text-blue`}>{fav.tags[0].name}</span>
                ) : (
                  <span className={`tag-badge bg-gray text-secondary`}>Uncategorized</span>
                )}
                <button 
                  className="icon-btn-small active-star" 
                  onClick={(e) => { e.stopPropagation(); toggleFavorite(fav.id); }}
                  title="Remove from favorites"
                >
                  <Star fill="#F59E0B" color="#F59E0B" size={18} />
                </button>
              </div>
              
              <h3 className="note-title">{fav.title || 'Untitled Note'}</h3>
              <p className="note-snippet">{stripHtml(fav.content).substring(0, 80)}...</p>
              
              <div className="note-footer">
                <div className="footer-flex-start text-secondary">
                  <FileText size={14} /> Note
                </div>
                <span className="note-time">{new Date(fav.updatedAt).toLocaleDateString()}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default FavoritesPage;
