import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

function ProjectDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const isAdmin = user?.role === 'ADMIN';

  const [project, setProject] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [allUsers, setAllUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  // modals
  const [showTaskModal, setShowTaskModal] = useState(false);
  const [showMemberModal, setShowMemberModal] = useState(false);
  const [editTask, setEditTask] = useState(null); // null = create, obj = edit

  const [taskForm, setTaskForm] = useState({
    title: '', description: '', dueDate: '', assignedToId: ''
  });

  const [selectedUser, setSelectedUser] = useState('');
  const [taskError, setTaskError] = useState('');

  useEffect(() => {
    loadAll();
  }, [id]);

  const loadAll = async () => {
    setLoading(true);
    try {
      const [projRes, taskRes] = await Promise.all([
        api.get(`/projects/${id}`),
        api.get(`/projects/${id}/tasks`)
      ]);
      setProject(projRes.data);
      setTasks(taskRes.data);

      if (isAdmin) {
        const usersRes = await api.get('/projects/users');
        setAllUsers(usersRes.data);
      }
    } catch (err) {
      console.log(err);
      if (err.response?.status === 404 || err.response?.status === 400) {
        navigate('/projects');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleTaskSubmit = async (e) => {
    e.preventDefault();
    setTaskError('');

    if (!taskForm.title.trim()) {
      setTaskError('Title is required');
      return;
    }

    const payload = {
      title: taskForm.title,
      description: taskForm.description,
      dueDate: taskForm.dueDate || null,
      assignedToId: taskForm.assignedToId ? parseInt(taskForm.assignedToId) : null
    };

    try {
      if (editTask) {
        await api.put(`/tasks/${editTask.id}`, payload);
      } else {
        await api.post(`/projects/${id}/tasks`, payload);
      }
      setShowTaskModal(false);
      setEditTask(null);
      setTaskForm({ title: '', description: '', dueDate: '', assignedToId: '' });
      loadAll();
    } catch (err) {
      setTaskError(err.response?.data?.error || 'Something went wrong');
    }
  };

  const handleStatusChange = async (taskId, newStatus) => {
    try {
      await api.put(`/tasks/${taskId}/status`, { status: newStatus });
      setTasks(tasks.map(t => t.id === taskId ? { ...t, status: newStatus } : t));
    } catch (err) {
      alert(err.response?.data?.error || 'Could not update status');
    }
  };

  const handleDeleteTask = async (taskId) => {
    if (!window.confirm('Delete this task?')) return;
    try {
      await api.delete(`/tasks/${taskId}`);
      setTasks(tasks.filter(t => t.id !== taskId));
    } catch (err) {
      alert(err.response?.data?.error || 'Delete failed');
    }
  };

  const openEditTask = (task) => {
    setEditTask(task);
    setTaskForm({
      title: task.title,
      description: task.description || '',
      dueDate: task.dueDate || '',
      assignedToId: task.assignedToId || ''
    });
    setShowTaskModal(true);
  };

  const handleAddMember = async () => {
    if (!selectedUser) return;
    try {
      await api.post(`/projects/${id}/members`, { userId: parseInt(selectedUser) });
      setSelectedUser('');
      setShowMemberModal(false);
      loadAll();
    } catch (err) {
      alert(err.response?.data?.error || 'Could not add member');
    }
  };

  const handleRemoveMember = async (memberId) => {
    if (!window.confirm('Remove this member from project?')) return;
    try {
      await api.delete(`/projects/${id}/members/${memberId}`);
      loadAll();
    } catch (err) {
      alert(err.response?.data?.error || 'Could not remove member');
    }
  };

  const getStatusClass = (task) => {
    if (task.overdue) return 'badge badge-overdue';
    if (task.status === 'DONE') return 'badge badge-done';
    if (task.status === 'IN_PROGRESS') return 'badge badge-in-progress';
    return 'badge badge-todo';
  };

  // members not already in project (for add member dropdown)
  const nonMembers = allUsers.filter(u =>
    !project?.members?.find(m => m.id === u.id)
  );

  if (loading) return <div className="container" style={{ marginTop: 20 }}>Loading...</div>;
  if (!project) return null;

  return (
    <div className="container">
      {/* Project Header */}
      <div className="page-header">
        <div>
          <h2>{project.name}</h2>
          <p style={{ color: '#666', fontSize: '14px', marginTop: '4px' }}>
            {project.description || 'No description'}
          </p>
        </div>
        {isAdmin && (
          <div style={{ display: 'flex', gap: '10px' }}>
            <button className="btn btn-secondary" onClick={() => setShowMemberModal(true)}>
              Manage Members
            </button>
            <button className="btn btn-primary" onClick={() => {
              setEditTask(null);
              setTaskForm({ title: '', description: '', dueDate: '', assignedToId: '' });
              setShowTaskModal(true);
            }}>
              + Add Task
            </button>
          </div>
        )}
      </div>

      {/* Members */}
      <div className="card" style={{ marginBottom: '20px' }}>
        <strong>Team Members ({project.memberCount})</strong>
        <div className="members-list">
          {project.members?.map(member => (
            <div key={member.id} className="member-chip">
              {member.name}
              {isAdmin && (
                <button onClick={() => handleRemoveMember(member.id)} title="Remove">✕</button>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Tasks */}
      <h3 style={{ marginBottom: '12px' }}>
        Tasks ({tasks.length})
      </h3>

      {tasks.length === 0 ? (
        <div className="card">
          <p style={{ color: '#888' }}>
            {isAdmin ? 'No tasks yet. Add a task to get started.' : 'No tasks assigned to you in this project.'}
          </p>
        </div>
      ) : (
        tasks.map(task => (
          <div key={task.id} className={`task-item ${task.overdue ? 'overdue' : ''}`}>
            <div style={{ flex: 1 }}>
              <strong>{task.title}</strong>
              {task.description && (
                <p style={{ fontSize: '13px', color: '#666', marginTop: '4px' }}>{task.description}</p>
              )}
              <div className="task-meta">
                {task.assignedToName ? `Assigned to: ${task.assignedToName}` : 'Unassigned'}
                {task.dueDate && ` | Due: ${task.dueDate}`}
              </div>
            </div>

            <div className="task-actions">
              <span className={getStatusClass(task)}>
                {task.overdue ? 'OVERDUE' : task.status.replace('_', ' ')}
              </span>

              {/* Members can update their own task status */}
              {(isAdmin || task.assignedToId === user?.userId) && (
                <select
                  className="status-select"
                  value={task.status}
                  onChange={e => handleStatusChange(task.id, e.target.value)}
                >
                  <option value="TODO">TODO</option>
                  <option value="IN_PROGRESS">IN PROGRESS</option>
                  <option value="DONE">DONE</option>
                </select>
              )}

              {isAdmin && (
                <>
                  <button
                    className="btn btn-secondary"
                    style={{ fontSize: '12px', padding: '4px 9px' }}
                    onClick={() => openEditTask(task)}
                  >
                    Edit
                  </button>
                  <button
                    className="btn btn-danger"
                    style={{ fontSize: '12px', padding: '4px 9px' }}
                    onClick={() => handleDeleteTask(task.id)}
                  >
                    Delete
                  </button>
                </>
              )}
            </div>
          </div>
        ))
      )}

      {/* Task Create/Edit Modal */}
      {showTaskModal && (
        <div className="modal-overlay">
          <div className="modal-box">
            <h3>{editTask ? 'Edit Task' : 'Add Task'}</h3>
            {taskError && <div className="error-msg">{taskError}</div>}
            <form onSubmit={handleTaskSubmit}>
              <div className="form-group">
                <label>Title *</label>
                <input
                  type="text"
                  value={taskForm.title}
                  onChange={e => setTaskForm({ ...taskForm, title: e.target.value })}
                  placeholder="Task title"
                />
              </div>
              <div className="form-group">
                <label>Description</label>
                <textarea
                  value={taskForm.description}
                  onChange={e => setTaskForm({ ...taskForm, description: e.target.value })}
                  placeholder="Brief description of task"
                />
              </div>
              <div className="form-group">
                <label>Due Date</label>
                <input
                  type="date"
                  value={taskForm.dueDate}
                  onChange={e => setTaskForm({ ...taskForm, dueDate: e.target.value })}
                />
              </div>
              <div className="form-group">
                <label>Assign To</label>
                <select
                  value={taskForm.assignedToId}
                  onChange={e => setTaskForm({ ...taskForm, assignedToId: e.target.value })}
                >
                  <option value="">-- Select Member --</option>
                  {project.members?.map(m => (
                    <option key={m.id} value={m.id}>{m.name}</option>
                  ))}
                </select>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => {
                  setShowTaskModal(false);
                  setTaskError('');
                }}>Cancel</button>
                <button type="submit" className="btn btn-primary">
                  {editTask ? 'Save Changes' : 'Create Task'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Add Member Modal */}
      {showMemberModal && (
        <div className="modal-overlay">
          <div className="modal-box">
            <h3>Add Member</h3>
            <div className="form-group">
              <label>Select User</label>
              <select value={selectedUser} onChange={e => setSelectedUser(e.target.value)}>
                <option value="">-- Choose a user --</option>
                {nonMembers.map(u => (
                  <option key={u.id} value={u.id}>{u.name} ({u.email})</option>
                ))}
              </select>
            </div>
            {nonMembers.length === 0 && (
              <p style={{ color: '#888', fontSize: '13px' }}>All registered users are already members.</p>
            )}
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowMemberModal(false)}>Close</button>
              <button className="btn btn-primary" onClick={handleAddMember} disabled={!selectedUser}>
                Add Member
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default ProjectDetail;
