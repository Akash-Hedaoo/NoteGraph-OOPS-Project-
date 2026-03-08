import React from 'react';
import { AlertTriangle } from 'lucide-react';

const ConfirmModal = ({ isOpen, title, message, confirmText = 'Delete', cancelText = 'Cancel', onConfirm, onCancel, danger = true }) => {
  if (!isOpen) return null;

  return (
    <div style={{
      position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.55)', display: 'flex', alignItems: 'center',
      justifyContent: 'center', zIndex: 999999, backdropFilter: 'blur(2px)'
    }}>
      <div style={{
        background: 'var(--bg-surface)', width: '400px', borderRadius: '16px',
        padding: '28px', boxShadow: '0 16px 48px rgba(0,0,0,0.2)',
        border: '1px solid var(--border-color)',
        animation: 'fadeInScale 0.15s ease-out'
      }}>
        {/* Icon */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '16px'
        }}>
          <div style={{
            width: '40px', height: '40px', borderRadius: '10px',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            background: danger ? 'rgba(239, 68, 68, 0.1)' : 'rgba(59, 130, 246, 0.1)',
            color: danger ? '#EF4444' : 'var(--primary-blue)',
            flexShrink: 0
          }}>
            <AlertTriangle size={20} />
          </div>
          <h3 style={{
            margin: 0, fontSize: '17px', fontWeight: '600',
            color: 'var(--text-main)', lineHeight: '1.3'
          }}>
            {title || 'Are you sure?'}
          </h3>
        </div>

        {/* Message */}
        <p style={{
          margin: '0 0 24px 0', fontSize: '14px', lineHeight: '1.6',
          color: 'var(--text-secondary)', paddingLeft: '52px'
        }}>
          {message}
        </p>

        {/* Actions */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
          <button
            onClick={onCancel}
            style={{
              padding: '9px 18px', borderRadius: '8px', fontSize: '14px',
              fontWeight: '500', cursor: 'pointer', border: '1px solid var(--border-color)',
              background: 'var(--bg-surface)', color: 'var(--text-main)',
              transition: 'all 0.15s ease'
            }}
            onMouseEnter={e => e.target.style.background = 'var(--bg-hover)'}
            onMouseLeave={e => e.target.style.background = 'var(--bg-surface)'}
          >
            {cancelText}
          </button>
          <button
            onClick={onConfirm}
            style={{
              padding: '9px 18px', borderRadius: '8px', fontSize: '14px',
              fontWeight: '500', cursor: 'pointer', border: 'none',
              background: danger ? '#EF4444' : 'var(--primary-blue)',
              color: '#fff', transition: 'all 0.15s ease'
            }}
            onMouseEnter={e => e.target.style.opacity = '0.85'}
            onMouseLeave={e => e.target.style.opacity = '1'}
          >
            {confirmText}
          </button>
        </div>
      </div>

      <style>{`
        @keyframes fadeInScale {
          from { opacity: 0; transform: scale(0.95); }
          to { opacity: 1; transform: scale(1); }
        }
      `}</style>
    </div>
  );
};

export default ConfirmModal;
