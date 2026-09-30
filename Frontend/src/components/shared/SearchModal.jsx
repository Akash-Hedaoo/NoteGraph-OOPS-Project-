import React, { useState, useEffect, useRef } from 'react';
import { Search, X, FileText, CheckCircle, ChevronDown, Folder, Calendar } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import './SearchModal.css';

const SearchModal = ({ isOpen, onClose }) => {
  const inputRef = useRef(null);
  const navigate = useNavigate();
  const { workspaceId } = useAuth();
  
  const [query, setQuery] = useState('');
  const [notes, setNotes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeFilter, setActiveFilter] = useState('all');

  useEffect(() => {
    if (isOpen && inputRef.current) {
      setTimeout(() => inputRef.current.focus(), 50);
    }
    
    if (isOpen && workspaceId && notes.length === 0) {
      const fetchNotes = async () => {
        setLoading(true);
        try {
          const res = await api.get(`/notes/workspace/${workspaceId}`);
          setNotes(res.data);
        } catch (err) {
          console.error("Failed to load notes for search", err);
        } finally {
          setLoading(false);
        }
      };
      fetchNotes();
    }
  }, [isOpen, workspaceId]);

  if (!isOpen) return null;

  const stripHtml = (html) => {
    if (!html) return '';
    let tmp = document.createElement("DIV");
    tmp.innerHTML = html;
    return tmp.textContent || tmp.innerText || "";
  };

  let resultNotes = [...notes];

  // Apply search query and tag filter
  if (query.trim()) {
    const q = query.toLowerCase();
    resultNotes = resultNotes.filter(n => {
      if (activeFilter === 'tags') {
        return n.tags && n.tags.some(t => t.name.toLowerCase().includes(q));
      }
      return (n.title || '').toLowerCase().includes(q) 
          || stripHtml(n.content).toLowerCase().includes(q)
          || (n.tags && n.tags.some(t => t.name.toLowerCase().includes(q)));
    });
  }

  // Apply sorting
  if (activeFilter === 'date') {
    resultNotes.sort((a, b) => new Date(b.updatedAt || b.createdAt).getTime() - new Date(a.updatedAt || a.createdAt).getTime());
  }

  const filteredNotes = resultNotes.slice(0, 10); // Limit to top 10 matches for preview

  const handleResultClick = (id) => {
    navigate(`/editor/${id}`);
    onClose();
  };

  const toggleFilter = (filter) => {
    setActiveFilter(prev => prev === filter ? 'all' : filter);
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="search-modal card" onClick={e => e.stopPropagation()}>
        <div className="search-modal-header">
           <Search size={20} className="text-secondary search-icon-large" />
           <input 
             ref={inputRef}
             type="text" 
             className="search-modal-input"
             value={query}
             onChange={e => setQuery(e.target.value)}
             placeholder="Search notes, tags..."
           />
           <button className="icon-btn-small" onClick={onClose}><X size={20} /></button>
        </div>

        <div className="search-filters-row">
           <div 
             className={`filter-pill ${activeFilter === 'tags' ? 'active' : ''}`} 
             onClick={() => toggleFilter('tags')}
           >
             <div className={`filter-color ${activeFilter === 'tags' ? 'blue' : 'gray'}`} style={activeFilter !== 'tags' ? {backgroundColor: 'var(--text-tertiary)', width: 12, height: 12, borderRadius: 4} : {}}></div> Tags
           </div>
           <div 
             className={`filter-pill ${activeFilter === 'date' ? 'active' : ''}`}
             onClick={() => toggleFilter('date')}
           >
             <Calendar size={14} className={activeFilter === 'date' ? 'text-blue' : 'text-secondary'} /> Date Modified
           </div>
           
           <div className="keyboard-hints">
             <span className="kbd">↵</span> to select <span className="kbd">Ctrl</span> + <span className="kbd">F</span> to focus
           </div>
        </div>

        <div className="search-results">
           <span className="results-label">BEST MATCHES</span>

           {loading ? (
             <div style={{padding: '1rem', color: 'var(--text-secondary)'}}>Loading notes...</div>
           ) : filteredNotes.length === 0 ? (
             <div style={{padding: '1rem', color: 'var(--text-secondary)'}}>No matching notes found.</div>
           ) : (
             filteredNotes.map((n, index) => (
               <div 
                 key={n.id} 
                 className={`result-item ${index === 0 && query.trim() !== '' ? 'highlight-result card' : ''}`} 
                 onClick={() => handleResultClick(n.id)}
               >
                 <div className={`result-icon ${index === 0 && query.trim() !== '' ? 'bg-blue-light text-blue' : 'bg-gray-light text-secondary'}`}>
                   <FileText size={18} />
                 </div>
                 <div className="result-content">
                   <div className="result-title-row">
                     <span className="r-title">{n.title || 'Untitled Note'}</span>
                     {n.tags && n.tags.length > 0 && (
                       <span className="r-badge badge-blue">{n.tags[0].name}</span>
                     )}
                   </div>
                   <p className="r-snippet">
                     {stripHtml(n.content).substring(0, 100)}...
                   </p>
                   <span className="r-meta"><Folder size={12} /> Uncategorized • Last modified: {new Date(n.updatedAt).toLocaleDateString()}</span>
                 </div>
               </div>
             ))
           )}
        </div>
        
        <div className="search-footer">
          <button className="view-all-results-btn">See all results {query ? `for "${query}"` : ''} →</button>
        </div>
        <div className="search-tip text-center">
            Tip: You can use search operators like <code>tag:work</code> or <code>is:task</code>
        </div>
      </div>
    </div>
  );
};

export default SearchModal;
