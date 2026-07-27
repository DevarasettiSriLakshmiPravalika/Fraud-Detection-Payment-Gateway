import React, { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

const Dashboard = () => {
  const { user } = useContext(AuthContext);

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <h1 style={{ fontSize: '2rem', fontWeight: '700', color: 'var(--primary)', marginBottom: '1rem' }}>Dashboard</h1>
      <div style={{ backgroundColor: 'var(--card-bg)', padding: '2rem', borderRadius: '16px', border: '1px solid var(--border-color)', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: '600', marginBottom: '1rem' }}>Welcome to FDPG</h2>
        <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>You are successfully authenticated and logged in.</p>
        
        <div style={{ padding: '1rem', backgroundColor: 'var(--bg-color)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
          <h3 style={{ fontSize: '0.875rem', fontWeight: '600', color: 'var(--text-primary)', marginBottom: '0.5rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>User Information</h3>
          <ul style={{ listStyle: 'none', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
            <li style={{ marginBottom: '0.5rem' }}><strong style={{ color: 'var(--text-primary)' }}>Username:</strong> {user?.username}</li>
            <li style={{ marginBottom: '0.5rem' }}><strong style={{ color: 'var(--text-primary)' }}>Email:</strong> {user?.email}</li>
            <li><strong style={{ color: 'var(--text-primary)' }}>Role:</strong> {user?.role}</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
