import React, { useState } from 'react';
import { X, FileBox, FileCode2, CheckCircle, FileType, Printer } from 'lucide-react';
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
    } else if (selectedFormat === 'md') {
      content = `# ${noteTitle || 'Untitled Note'}\n\n${stripHtml(noteContent)}`;
      mimeType = 'text/markdown';
      filename += '.md';
    } else if (selectedFormat === 'pdf') {
      // Use browser print-to-PDF via hidden iframe
      const htmlContent = `<!DOCTYPE html><html><head><title>${noteTitle || 'Untitled Note'}</title><style>body{max-width:800px;margin:0 auto;padding:2rem;font-family:'Segoe UI',sans-serif;color:#222;line-height:1.6}h1{font-size:24px;margin-bottom:16px;color:#111}h2{font-size:20px}h3{font-size:17px}p{margin:8px 0}ul,ol{padding-left:24px}@media print{body{padding:0}}</style></head><body><h1>${noteTitle || 'Untitled Note'}</h1>${noteContent || ''}</body></html>`;
      const printFrame = document.createElement('iframe');
      printFrame.style.position = 'fixed';
      printFrame.style.right = '0';
      printFrame.style.bottom = '0';
      printFrame.style.width = '0';
      printFrame.style.height = '0';
      printFrame.style.border = '0';
      document.body.appendChild(printFrame);
      printFrame.contentDocument.write(htmlContent);
      printFrame.contentDocument.close();
      printFrame.contentWindow.focus();
      setTimeout(() => {
        printFrame.contentWindow.print();
        setTimeout(() => document.body.removeChild(printFrame), 1000);
      }, 250);
      setShowToast(true);
      setTimeout(() => { onClose(); setShowToast(false); }, 2000);
      return;
    } else if (selectedFormat === 'doc') {
      const docHtml = `<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'><head><meta charset='utf-8'><title>${noteTitle || 'Untitled Note'}</title><style>body{font-family:'Calibri',sans-serif;padding:1in;line-height:1.5;color:#222}h1{font-size:22pt;color:#111;margin-bottom:12pt}h2{font-size:16pt}p{margin:6pt 0}ul,ol{padding-left:24pt}</style></head><body><h1>${noteTitle || 'Untitled Note'}</h1>${noteContent || ''}</body></html>`;
      content = docHtml;
      mimeType = 'application/msword';
      filename += '.doc';
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

              <div 
                className={`export-option card ${selectedFormat === 'pdf' ? 'selected' : ''}`}
                onClick={() => setSelectedFormat('pdf')}
              >
                <div className="radio-circle">
                  {selectedFormat === 'pdf' && <div className="radio-inner" />}
                </div>
                <div className="export-option-content">
                  <div className="e-title"><Printer size={18} className="text-blue" /> PDF Document</div>
                  <p className="e-desc">Print-ready format. Opens your browser's Save as PDF dialog.</p>
                </div>
              </div>

              <div 
                className={`export-option card ${selectedFormat === 'doc' ? 'selected' : ''}`}
                onClick={() => setSelectedFormat('doc')}
              >
                <div className="radio-circle">
                  {selectedFormat === 'doc' && <div className="radio-inner" />}
                </div>
                <div className="export-option-content">
                  <div className="e-title"><FileType size={18} className="text-blue" /> Word Document (.doc)</div>
                  <p className="e-desc">Compatible with Microsoft Word and Google Docs for further editing.</p>
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
