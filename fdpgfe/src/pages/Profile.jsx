import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../context/AuthContext';
import { Copy, Check, User, CreditCard, Wallet, Shield } from 'lucide-react';
import paymentService from '../services/paymentService';
import styles from './Profile.module.css';

const Profile = () => {
  const { user } = useContext(AuthContext);
  const [account, setAccount] = useState(null);
  const [loading, setLoading] = useState(true);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    paymentService.getMyAccount()
      .then(res => { if (res.data.success) setAccount(res.data.data); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const copyAccountNumber = () => {
    if (!account?.accountNumber) return;
    navigator.clipboard.writeText(account.accountNumber);
    setCopied(true);
    setTimeout(() => setCopied(false), 1800);
  };

  const formatBalance = (balance, currency) => {
    const symbol = currency === 'INR' ? '₹' : '$';
    return `${symbol}${Number(balance).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  const formatDate = (date) =>
    new Date(date).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>
        <h1 className={styles.title}>Profile</h1>
        <p className={styles.subtitle}>Your account details and virtual banking information</p>
      </div>

      <div className={styles.grid}>
        {/* Left Column: Identity */}
        <div className={styles.leftCol}>
          {/* Avatar Card */}
          <div className={styles.avatarCard}>
            <div className={styles.avatar}>
              {user?.username?.charAt(0).toUpperCase()}
            </div>
            <h2 className={styles.displayName}>{user?.username}</h2>
            <p className={styles.displayEmail}>{user?.email}</p>
            <span className={styles.rolePill}>{user?.role?.replace('ROLE_', '')}</span>
          </div>

          {/* Account Status */}
          {!loading && account && (
            <div className={styles.statusCard}>
              <div className={styles.statusRow}>
                <span className={styles.statusLabel}>Account Status</span>
                <span className={`${styles.statusValue} ${account.accountStatus === 'ACTIVE' ? styles.active : styles.inactive}`}>
                  <span className={styles.statusDot} />
                  {account.accountStatus}
                </span>
              </div>
              <div className={styles.statusRow}>
                <span className={styles.statusLabel}>Member Since</span>
                <span className={styles.statusValue}>
                  {account.createdAt ? formatDate(account.createdAt) : '—'}
                </span>
              </div>
            </div>
          )}
        </div>

        {/* Right Column: Details */}
        <div className={styles.rightCol}>
          {/* Identity Details */}
          <div className={styles.detailSection}>
            <div className={styles.sectionHeader}>
              <User size={16} color="var(--accent)" />
              <h3 className={styles.sectionTitle}>Identity</h3>
            </div>
            <div className={styles.detailList}>
              <DetailItem label="Username" value={user?.username} />
              <DetailItem label="Email" value={user?.email} />
              <DetailItem label="Role" value={user?.role?.replace('ROLE_', '')} />
            </div>
          </div>

          {/* Virtual Account */}
          <div className={styles.detailSection}>
            <div className={styles.sectionHeader}>
              <CreditCard size={16} color="#6366F1" />
              <h3 className={styles.sectionTitle}>Virtual Account</h3>
            </div>

            {loading ? (
              <div className={styles.skeleton} />
            ) : account ? (
              <div className={styles.detailList}>
                <div className={styles.detailItem}>
                  <span className={styles.detailLabel}>Account Number</span>
                  <div className={styles.acctRow}>
                    <span className={styles.acctNumber} id="account-number-display">
                      {account.accountNumber}
                    </span>
                    <button
                      className={styles.copyBtn}
                      onClick={copyAccountNumber}
                      title="Copy account number"
                      id="copy-account-btn"
                    >
                      {copied ? <Check size={14} color="var(--accent)" /> : <Copy size={14} />}
                    </button>
                  </div>
                </div>
                <DetailItem label="Currency" value={account.currency} />
                <DetailItem label="Account Status" value={account.accountStatus} />
              </div>
            ) : (
              <p className={styles.noAccount}>No account found.</p>
            )}
          </div>

          {/* Balance */}
          <div className={styles.balanceSection}>
            <div className={styles.sectionHeader}>
              <Wallet size={16} color="var(--accent)" />
              <h3 className={styles.sectionTitle}>Available Balance</h3>
            </div>
            {loading ? (
              <div className={styles.skeleton} />
            ) : account ? (
              <div className={styles.balanceDisplay} id="balance-display">
                <span className={styles.balanceAmount}>
                  {formatBalance(account.balance, account.currency)}
                </span>
                <span className={styles.balanceCurrency}>{account.currency} · Virtual Balance</span>
              </div>
            ) : (
              <p className={styles.noAccount}>—</p>
            )}
          </div>

          {/* Security */}
          <div className={styles.detailSection}>
            <div className={styles.sectionHeader}>
              <Shield size={16} color="#F59E0B" />
              <h3 className={styles.sectionTitle}>Security</h3>
            </div>
            <div className={styles.detailList}>
              <DetailItem label="Authentication" value="JWT Token" />
              <DetailItem label="Password" value="••••••••" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

const DetailItem = ({ label, value }) => (
  <div className={styles.detailItem}>
    <span className={styles.detailLabel}>{label}</span>
    <span className={styles.detailValue}>{value || '—'}</span>
  </div>
);

export default Profile;
