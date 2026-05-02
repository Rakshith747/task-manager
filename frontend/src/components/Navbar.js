import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="navbar">
      <Link to="/" className="brand">Team Task Manager</Link>

      <nav>
        <Link to="/">Dashboard</Link>
        <Link to="/projects">Projects</Link>
      </nav>

      <div style={{ display: 'flex', alignItems: 'center' }}>
        <span className="user-info">
          {user?.name} ({user?.role})
        </span>
        <button onClick={handleLogout}>Logout</button>
      </div>
    </div>
  );
}

export default Navbar;
