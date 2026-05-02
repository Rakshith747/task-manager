import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

function Projects() {
  const { user } = useAuth();
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [newProject, setNewProject] = useState({ name: '', description: '' });
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const isAdmin = user?.role === 'ADMIN';

  useEffect(() => {
    loadProjects();
  }, []);

  const loadProjects = async () => {
    try {
      const res = await api.get('/projects');
      setProjects(res.data);
    } catch (err) {
      console.log('Error loading projects');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!newProject.name.trim()) {
      setError('Project name is required');
      return;
    }

    try {
      await api.post('/projects', newProject);
      setShowModal(false);
      setNewProject({ name: '', description: '' });
      setError('');
      loadProjects();
    } catch (err) {
      setError(err.response?.data?.error || 'Could not create project');
    }
  };

  const handleDelete = async (e, id) => {
    e.stopPropagation();
    if (!window.confirm('Delete this project?')) return;

    try {
      await api.delete(`/projects/${id}`);
      setProjects(projects.filter(p => p.id !== id));
    } catch (err) {
      alert(err.response?.data?.error || 'Delete failed');
    }
  };

  if (loading) return <div className="container" style={{ marginTop: 20 }}>Loading projects...</div>;

  return (
    <div className="container">
      <div className="page-header">
        <h2>Projects</h2>
        {isAdmin && (
          <button className="btn btn-primary" onClick={() => setShowModal(true)}>
            + New Project
          </button>
        )}
      </div>

      {projects.length === 0 ? (
        <div className="card">
          <p style={{ color: '#888' }}>
            {isAdmin ? 'No projects yet. Create one to get started.' : 'You are not assigned to any projects.'}
          </p>
        </div>
      ) : (
        <div className="projects-grid">
          {projects.map(project => (
            <div
              key={project.id}
              className="project-card"
              onClick={() => navigate(`/projects/${project.id}`)}
            >
              <h3>{project.name}</h3>
              <p>{project.description || 'No description'}</p>
              <div className="meta">
                <span>👤 {project.memberCount} members</span>
                <span>📋 {project.taskCount} tasks</span>
              </div>
              <div style={{ marginTop: '10px', fontSize: '12px', color: '#999' }}>
                Created by: {project.createdByName}
              </div>
              {isAdmin && (
                <button
                  className="btn btn-danger"
                  style={{ marginTop: '12px', fontSize: '12px', padding: '5px 10px' }}
                  onClick={(e) => handleDelete(e, project.id)}
                >
                  Delete
                </button>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Create project modal */}
      {showModal && (
        <div className="modal-overlay">
          <div className="modal-box">
            <h3>Create New Project</h3>
            {error && <div className="error-msg">{error}</div>}
            <form onSubmit={handleCreate}>
              <div className="form-group">
                <label>Project Name *</label>
                <input
                  type="text"
                  value={newProject.name}
                  onChange={e => setNewProject({ ...newProject, name: e.target.value })}
                  placeholder="e.g. College ERP System"
                />
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea
                  value={newProject.description}
                  onChange={e => setNewProject({ ...newProject, description: e.target.value })}
                  placeholder="What is this project about?"
                />
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => {
                  setShowModal(false);
                  setError('');
                }}>Cancel</button>
                <button type="submit" className="btn btn-primary">Create</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Projects;
