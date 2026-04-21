import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Mail, Github, Eye, EyeOff, AlertCircle, ArrowRight, User as UserIcon } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import './Login.css';

const Login = () => {
  const navigate = useNavigate();
  const { login, register } = useAuth();
  
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  
  const [showPassword, setShowPassword] = useState(false);
  const [errorText, setErrorText] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorText('');

    if (!email || !password || (!isLogin && !name)) {
      setErrorText('Please fill in all required fields.');
      return;
    }

    try {
      if (isLogin) {
        await login(email, password);
      } else {
        await register(email, password, name);
      }
      navigate('/dashboard');
    } catch (err) {
      setErrorText(err.response?.data?.message || 'Authentication failed. Please check your credentials.');
    }
  };

  return (
    <div className="login-container">
      {/* Top Header */}
      <header className="login-header">
        <div className="logo-section">
          <div className="logo-icon">
            <svg viewBox="0 0 24 24" fill="var(--primary-blue)" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" />
              <path d="M2 17L12 22L22 17" stroke="var(--primary-blue)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="var(--primary-blue)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <span className="logo-text">NoteGraph</span>
        </div>
        <div className="header-links">
          <a href="#" className="help-link">Help Center</a>
          <a href="#" className="support-link">Contact Support</a>
        </div>
      </header>

      {/* Main Form */}
      <main className="login-main">
        <div className="login-box card">
          <h1 className="welcome-title">{isLogin ? 'Welcome Back' : 'Create an Account'}</h1>
          <p className="welcome-subtitle">
            {isLogin ? 'Sign in to sync your notes across devices' : 'Get started with your digital brain'}
          </p>

          <form onSubmit={handleSubmit} className="login-form">
            {!isLogin && (
              <div className="form-group">
                <label>Full Name</label>
                <div className="input-wrapper">
                  <input 
                    type="text" 
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="input-field" 
                    placeholder="John Doe"
                  />
                  <UserIcon className="input-icon" size={18} />
                </div>
              </div>
            )}

            <div className="form-group">
              <label>Email</label>
              <div className="input-wrapper">
                <input 
                  type="email" 
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="input-field" 
                  placeholder="name@company.com"
                />
                <Mail className="input-icon" size={18} />
              </div>
            </div>

            <div className="form-group">
              <div className="password-header">
                <label>Password</label>
                {isLogin && <a href="#" className="forgot-link">Forgot password?</a>}
              </div>
              <div className={`input-wrapper ${errorText ? 'error' : ''}`}>
                <input 
                  type={showPassword ? "text" : "password"} 
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="input-field" 
                  placeholder="••••••••••••••••"
                />
                <button 
                  type="button" 
                  className="input-icon-btn"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              {errorText && (
                <div className="error-message" style={{ marginTop: '8px', color: 'var(--text-error)', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px' }}>
                  <AlertCircle size={14} />
                  <span>{errorText}</span>
                </div>
              )}
            </div>

            <button type="submit" className="btn-primary login-submit-btn">
              {isLogin ? 'Sign In' : 'Sign Up'} <ArrowRight size={18} />
            </button>
          </form>

          <div className="divider">
            <span>Or continue with</span>
          </div>

          <div className="social-login">
            <button className="btn-outline social-btn">
              <img src="https://upload.wikimedia.org/wikipedia/commons/5/53/Google_%22G%22_Logo.svg" alt="Google" width="18" height="18" />
              Google
            </button>
            <button className="btn-outline social-btn">
              <Github size={18} />
              GitHub
            </button>
          </div>

          <div className="signup-prompt">
            {isLogin ? "Don't have an account? " : "Already have an account? "}
            <a 
              href="#" 
              onClick={(e) => { e.preventDefault(); setIsLogin(!isLogin); setErrorText(''); }} 
              className="signup-link"
            >
              {isLogin ? 'Sign up' : 'Sign in'}
            </a>
          </div>
        </div>

        <footer className="login-footer">
          <a href="#">Privacy Policy</a>
          <a href="#">Terms of Service</a>
        </footer>
      </main>
    </div>
  );
};

export default Login;
