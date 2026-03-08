import React, { useState } from 'react';
import { X, FileBox, FileCode2, CheckCircle } from 'lucide-react';
import './ExportModal.css';

const ExportModal = ({ isOpen, onClose, noteTitle, noteContent }) => {
  const [selectedFormat, setSelectedFormat] = useState('html');
  const [showToast, setShowToast] = useState(false);

  if (!isOpen) {
    if (showToast) setShowToast(false);
    return null;
  }

  const stripHtml = (html) => {
    if (!html) return '';
    let tmp = document.createElement("DIV");
    tmp.innerHTML = html;
    return tmp.textContent || tmp.innerText || "";
  };

  const handleDownload = () => {
    let content = '';
    let mimeType = '';
    let filename = (noteTitle || 'Untitled').replace(/[^a-z0-9]/gi, '_').toLowerCase();

    if (selectedFormat === 'html') {
      content = `<!DOCTYPE html><html><head><title>${noteTitle}</title></head><body style="max-width:800px;margin:0 auto;padding:2rem;font-family:sans-serif;"><h1>${noteTitle || 'Untitled Note'}</h1>\n${noteContent || ''}</body></html>`;
      mimeType = 'text/html';
      filename += '.html';
    } else {
      content = `# ${noteTitle || 'Untitled Note'}\n\n${stripHtml(noteContent)}`;
      mimeType = 'text/markdown';
      filename += '.md';
    }

    const blob = new Blob([content], { type: mimeType });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);

    setShowToast(true);
    setTimeout(() => {
      onClose();
      setShowToast(false);
    }, 2000);
  };

  return (
    <>
      <div className="modal-backdrop" onClick={onClose}>
        <div className="export-modal card" onClick={e => e.stopPropagation()}>
          <div className="modal-header-simple">
            <h2 className="modal-title">Export Note</h2>
            <button className="icon-btn-small" onClick={onClose}><X size={20} /></button>
          </div>
          
          <div className="modal-body">
            <p className="modal-subtitle">Choose a format to export "{noteTitle || 'Untitled'}".</p>
            
            <div className="export-options">
              <div 
                className={`export-option card ${selectedFormat === 'html' ? 'selected' : ''}`}
                onClick={() => setSelectedFormat('html')}
              >
                <div className="radio-circle">
                  {selectedFormat === 'html' && <div className="radio-inner" />}
                </div>
                <div className="export-option-content">
                  <div className="e-title"><FileBox size={18} className="text-blue" /> HTML Document</div>
                  <p className="e-desc">Best for sharing and viewing in any browser. Preserves visual formatting.</p>
                </div>
              </div>

              <div 
                className={`export-option card ${selectedFormat === 'md' ? 'selected' : ''}`}
                onClick={() => setSelectedFormat('md')}
              >
                <div className="radio-circle">
                  {selectedFormat === 'md' && <div className="radio-inner" />}
                </div>
                <div className="export-option-content">
                  <div className="e-title"><FileCode2 size={18} className="text-blue" /> Markdown File</div>
                  <p className="e-desc">Plain text with lightweight formatting. Best for use in other editors.</p>
                </div>
              </div>
            </div>
          </div>
          
          <div className="modal-footer-actions">
             <button className="btn-outline" onClick={onClose}>Cancel</button>
             <button className="btn-primary" onClick={handleDownload}>Download</button>
          </div>
        </div>
      </div>

      {showToast && (
        <div className="toast-notification notification-dark">
          <div className="toast-icon-wrapper bg-green">
            <CheckCircle size={16} color="white" />
          </div>
          <div className="toast-content">
            <span className="toast-title">Export Complete</span>
            <span className="toast-desc">Note downloaded to your filesystem.</span>
          </div>
          <button className="icon-btn-small toast-close" onClick={() => setShowToast(false)}>
            <X size={16} />
          </button>
        </div>
      )}
    </>
  );
};

export default ExportModal;
