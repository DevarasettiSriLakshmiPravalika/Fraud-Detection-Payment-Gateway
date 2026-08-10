import React, { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { LogOut, User, CreditCard, List, LayoutDashboard, UserCircle } from 'lucide-react';
import { useNavigate, NavLink } from 'react-router-dom';
import styles from './MainLayout.module.css';

const MainLayout = ({ children }) => {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className={styles.root}>
      <header className={styles.header}>
        <div className={styles.logo}>
          <span className={styles.logoAccent}>FD</span>PG
        </div>

        <nav className={styles.nav}>
          <NavLink
            to="/dashboard"
            className={({ isActive }) => `${styles.navLink} ${isActive ? styles.navActive : ''}`}
            id="nav-dashboard"
          >
            <LayoutDashboard size={16} />
            Dashboard
          </NavLink>
          <NavLink
            to="/payments/new"
            className={({ isActive }) => `${styles.navLink} ${isActive ? styles.navActive : ''}`}
            id="nav-make-payment"
          >
            <CreditCard size={16} />
            Make Payment
          </NavLink>
          <NavLink
            to="/payments"
            end
            className={({ isActive }) => `${styles.navLink} ${isActive ? styles.navActive : ''}`}
            id="nav-history"
          >
            <List size={16} />
            History
          </NavLink>
          <NavLink
            to="/profile"
            className={({ isActive }) => `${styles.navLink} ${isActive ? styles.navActive : ''}`}
            id="nav-profile"
          >
            <UserCircle size={16} />
            Profile
          </NavLink>
        </nav>

        {user && (
          <div className={styles.userArea}>
            <div className={styles.userInfo}>
              <div className={styles.avatar}>
                {user.username?.charAt(0).toUpperCase()}
              </div>
              <div className={styles.userMeta}>
                <span className={styles.userName}>{user.username}</span>
                <span className={styles.userEmail}>{user.email}</span>
              </div>
            </div>
            <button
              onClick={handleLogout}
              className={styles.logoutBtn}
              id="logout-btn"
              title="Logout"
            >
              <LogOut size={16} />
            </button>
          </div>
        )}
      </header>

      <main className={styles.main}>
        {children}
      </main>
    </div>
  );
};

export default MainLayout;
