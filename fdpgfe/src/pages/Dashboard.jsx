import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import { CreditCard, List, ArrowRight, Clock, CheckCircle, Copy, Check, Wallet, User } from 'lucide-react';
import paymentService from '../services/paymentService';
import styles from './Dashboard.module.css';

const Dashboard = () => {
  const { user } = useContext(AuthContext);
  const [account, setAccount] = useState(null);
  const [recentPayments, setRecentPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [accountRes, paymentsRes] = await Promise.all([
          paymentService.getMyAccount(),
          paymentService.getUserPayments(),
        ]);
        if (accountRes.data.success) setAccount(accountRes.data.data);
        if (paymentsRes.data.success) setRecentPayments(paymentsRes.data.data.slice(0, 5));
      } catch {
        // silent fail
      } finally {
        setLoading(false);
      }
    };
    fetchData();
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

  const formatAmount = (amount, currency) => {
    const symbol = currency === 'INR' ? '₹' : '$';
    return `${symbol}${Number(amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
  };

  const formatDate = (date) =>
    new Date(date).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });

  return (
    <div className={styles.container}>
      {/* Welcome Banner */}
      <div className={styles.welcomeBanner}>
        <div>
          <h1 className={styles.welcomeTitle}>
            Welcome back, <span className={styles.username}>{user?.username}</span>
          </h1>
          <p className={styles.welcomeSub}>{user?.email}</p>
        </div>
        <div className={styles.roleBadge}>{user?.role?.replace('ROLE_', '')}</div>
      </div>

      {/* Stats Row */}
      <div className={styles.statsRow}>
        {/* Balance Card */}
        <div className={styles.statCard} id="balance-card">
          <div className={styles.statHeader}>
            <div className={styles.statIcon} style={{ background: 'rgba(16,185,129,0.12)' }}>
              <Wallet size={20} color="var(--accent)" />
            </div>
            <span className={styles.statLabel}>Available Balance</span>
          </div>
          {loading ? (
            <div className={styles.statSkeleton} />
          ) : (
            <>
              <div className={styles.balanceAmount}>
                {account ? formatBalance(account.balance, account.currency) : '—'}
              </div>
              <div className={styles.balanceCurrency}>{account?.currency || 'INR'} · Virtual Account</div>
            </>
          )}
        </div>

        {/* Account Number Card */}
        <div className={styles.statCard} id="account-number-card">
          <div className={styles.statHeader}>
            <div className={styles.statIcon} style={{ background: 'rgba(99,102,241,0.12)' }}>
              <CreditCard size={20} color="#6366F1" />
            </div>
            <span className={styles.statLabel}>Account Number</span>
          </div>
          {loading ? (
            <div className={styles.statSkeleton} />
          ) : (
            <>
              <div className={styles.accountNumberRow}>
                <span className={styles.accountNumber}>{account?.accountNumber || '—'}</span>
                {account && (
                  <button
                    className={styles.copyBtn}
                    onClick={copyAccountNumber}
                    title="Copy account number"
                    id="copy-account-btn"
                  >
                    {copied ? <Check size={15} color="var(--accent)" /> : <Copy size={15} />}
                  </button>
                )}
              </div>
              <div className={styles.accountStatus}>
                {account && (
                  <span className={`${styles.statusDot} ${account.accountStatus === 'ACTIVE' ? styles.active : styles.inactive}`} />
                )}
                {account?.accountStatus || 'Unknown'}
              </div>
            </>
          )}
        </div>

        {/* Profile Card */}
        <div className={styles.statCard} id="profile-card">
          <div className={styles.statHeader}>
            <div className={styles.statIcon} style={{ background: 'rgba(245,158,11,0.12)' }}>
              <User size={20} color="#F59E0B" />
            </div>
            <span className={styles.statLabel}>Profile</span>
          </div>
          <div className={styles.profileName}>{user?.username}</div>
          <div className={styles.profileEmail}>{user?.email}</div>
          <Link to="/profile" className={styles.profileLink}>View Profile →</Link>
        </div>
      </div>

      {/* Quick Actions */}
      <div className={styles.quickActions}>
        <Link to="/payments/new" className={styles.actionCard} id="make-payment-card">
          <div className={styles.actionIcon} style={{ backgroundColor: 'rgba(16,185,129,0.12)' }}>
            <CreditCard size={24} color="var(--accent)" />
          </div>
          <div>
            <h3 className={styles.actionTitle}>Make Payment</h3>
            <p className={styles.actionSub}>Send money using account number</p>
          </div>
          <ArrowRight size={18} className={styles.actionArrow} />
        </Link>

        <Link to="/payments" className={styles.actionCard} id="payment-history-card">
          <div className={styles.actionIcon} style={{ backgroundColor: 'rgba(99,102,241,0.12)' }}>
            <List size={24} color="#6366F1" />
          </div>
          <div>
            <h3 className={styles.actionTitle}>Payment History</h3>
            <p className={styles.actionSub}>View all transactions</p>
          </div>
          <ArrowRight size={18} className={styles.actionArrow} />
        </Link>
      </div>

      {/* Recent Transactions */}
      <div className={styles.section}>
        <div className={styles.sectionHeader}>
          <h2 className={styles.sectionTitle}>Recent Transactions</h2>
          <Link to="/payments" className={styles.viewAll}>View all</Link>
        </div>

        {loading ? (
          <div className={styles.loadingState}>
            <div className={styles.spinner} />
            <p>Loading transactions...</p>
          </div>
        ) : recentPayments.length === 0 ? (
          <div className={styles.emptyState}>
            <Clock size={40} color="var(--text-secondary)" />
            <p>No transactions yet.</p>
            <Link to="/payments/new" className={styles.emptyLink}>Make your first payment →</Link>
          </div>
        ) : (
          <div className={styles.transactionList}>
            {recentPayments.map((tx) => {
              const isSent = tx.transactionType === 'SENT';
              return (
                <Link
                  to={`/payments/${tx.transactionId}`}
                  key={tx.transactionId}
                  className={styles.transactionRow}
                  id={`tx-${tx.transactionId}`}
                >
                  <div className={`${styles.txIcon} ${isSent ? styles.txIconSent : styles.txIconReceived}`}>
                    {isSent
                      ? <CheckCircle size={18} color="#EF4444" />
                      : <CheckCircle size={18} color="var(--accent)" />}
                  </div>
                  <div className={styles.txInfo}>
                    <span className={styles.txReceiver}>{tx.counterpartyName}</span>
                    <span className={styles.txAccount}>{tx.counterpartyAccountNumber}</span>
                    <span className={styles.txDate}>{formatDate(tx.createdAt)}</span>
                  </div>
                  <div className={styles.txRight}>
                    <span className={isSent ? styles.txAmountDebit : styles.txAmountCredit}>
                      {isSent ? '−' : '+'}{formatAmount(tx.amount, tx.currency)}
                    </span>
                    <span className={styles.statusBadge}>{tx.status}</span>
                  </div>
                </Link>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
