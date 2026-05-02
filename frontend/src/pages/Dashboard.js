import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

function Dashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [recentTasks, setRecentTasks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const statsRes = await api.get('/dashboard');
      setStats(statsRes.data);

      // get recent projects and tasks
      const projectsRes = await api.get('/projects');
      const projects = projectsRes.data.slice(0, 3); // just first few

      const allTasks = [];
      for (let p of projects) {
        try {
          const taskRes = await api.get(`/projects/${p.id}/tasks`);
          allTasks.push(...taskRes.data);
        } catch (e) {
          // skip
        }
      }

      // sort by due date, show upcoming ones first
      const sorted = allTasks.sort((a, b) => {
        if (!a.dueDate) return 1;
        if (!b.dueDate) return -1;
        return new Date(a.dueDate) - new Date(b.dueDate);
      });

      setRecentTasks(sorted.slice(0, 5));
    } catch (err) {
      console.log('Dashboard error:', err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (task) => {
    if (task.overdue) return <span className="badge badge-overdue">OVERDUE</span>;
    if (task.status === 'DONE') return <span className="badge badge-done">DONE</span>;
    if (task.status === 'IN_PROGRESS') return <span className="badge badge-in-progress">IN PROGRESS</span>;
    return <span className="badge badge-todo">TODO</span>;
  };

  if (loading) return <div className="container" style={{ marginTop: 20 }}>Loading...</div>;

  return (
    <div className="container">
      <div className="page-header">
        <h2>Dashboard</h2>
        <span style={{ color: '#666', fontSize: '14px' }}>Welcome back, {user?.name}!</span>
      </div>

      {stats && (
        <div className="stats-grid">
          <div className="stat-card">
            <div className="number">{stats.total}</div>
            <div className="label">Total Tasks</div>
          </div>
          <div className="stat-card completed">
            <div className="number">{stats.completed}</div>
            <div className="label">Completed</div>
          </div>
          <div className="stat-card">
            <div className="number">{stats.pending}</div>
            <div className="label">Pending</div>
          </div>
          <div className="stat-card overdue">
            <div className="number">{stats.overdue}</div>
            <div className="label">Overdue</div>
          </div>
        </div>
      )}

      <div className="card">
        <h3 style={{ marginBottom: '14px' }}>Recent Tasks</h3>

        {recentTasks.length === 0 ? (
          <p style={{ color: '#888' }}>No tasks assigned yet. <Link to="/projects">View projects</Link></p>
        ) : (
          recentTasks.map(task => (
            <div key={task.id} className={`task-item ${task.overdue ? 'overdue' : ''}`}>
              <div>
                <strong>{task.title}</strong>
                <div className="task-meta">
                  Project: {task.projectName} &nbsp;|&nbsp;
                  {task.dueDate ? `Due: ${task.dueDate}` : 'No due date'}
                </div>
              </div>
              <div>
                {getStatusBadge(task)}
              </div>
            </div>
          ))
        )}
      </div>

      <div style={{ marginTop: '16px' }}>
        <Link to="/projects">
          <button className="btn btn-primary">View All Projects →</button>
        </Link>
      </div>
    </div>
  );
}

export default Dashboard;
