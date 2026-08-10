import React, { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, Copy, Check } from 'lucide-react';
import paymentService from '../services/paymentService';
import styles from './TransactionDetail.module.css';

const TransactionDetail = () => {
  const { transactionId } = useParams();
  const navigate = useNavigate();
  const [transaction, setTransaction] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    const fetchDetail = async () => {
      try {
        const res = await paymentService.getPaymentDetail(transactionId);
        if (res.data.success) {
          setTransaction(res.data.data);
        } else {
          setError(res.data.message);
        }
      } catch (err) {
        setError(err.response?.data?.message || 'Transaction not found');
      } finally {
        setLoading(false);
      }
    };
    fetchDetail();
  }, [transactionId]);

  const copyId = () => {
    navigator.clipboard.writeText(transaction.transactionId);
    setCopied(true);
    setTimeout(() => setCopied(false), 1800);
  };

  const formatAmount = (amount, currency) => {
    const symbol = currency === 'INR' ? '₹' : '$';
    return `${symbol}${Number(amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
  };

  const formatDateTime = (date) =>
    new Date(date).toLocaleString('en-US', {
      weekday: 'short', month: 'long', day: 'numeric', year: 'numeric',
      hour: '2-digit', minute: '2-digit', second: '2-digit',
    });

  if (loading) {
    return (
      <div className={styles.centered}>
        <div className={styles.spinner} />
        <p>Loading transaction...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.container}>
        <button className={styles.backBtn} onClick={() => navigate('/payments')} id="back-btn">
          <ArrowLeft size={16} /> Back to History
        </button>
        <div className={styles.errorCard}>
          <h2>Transaction Not Found</h2>
          <p>{error}</p>
          <Link to="/payments" className={styles.linkBtn}>← Return to Payment History</Link>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <button className={styles.backBtn} onClick={() => navigate('/payments')} id="back-btn">
        <ArrowLeft size={16} /> Back to History
      </button>

      {/* Header Card */}
      <div className={styles.headerCard}>
        <div>
          <p className={styles.headerLabel}>Transaction ID</p>
          <div className={styles.txIdRow}>
            <span className={styles.txIdText}>{transaction.transactionId}</span>
            <button
              className={styles.copyBtn}
              onClick={copyId}
              title="Copy ID"
              id="copy-id-btn"
            >
              {copied ? <Check size={15} color="var(--accent)" /> : <Copy size={15} />}
            </button>
          </div>
        </div>
        <div className={styles.amountBlock}>
          <span className={styles.amountLabel}>Amount Transferred</span>
          <span className={styles.amountValue}>
            {formatAmount(transaction.amount, transaction.currency)}
          </span>
          <span className={styles.statusBadge}>{transaction.status}</span>
        </div>
      </div>

      {/* Details Grid */}
      <div className={styles.detailsGrid}>
        {/* Sender */}
        <div className={styles.detailSection}>
          <h3 className={styles.sectionLabel}>Sender</h3>
          <div className={styles.detailCard}>
            <DetailRow label="Username" value={transaction.senderUsername} />
            <DetailRow label="Email" value={transaction.senderEmail} />
            <DetailRow label="Account">
              <span className={styles.acctNumber}>{transaction.senderAccountNumber}</span>
            </DetailRow>
          </div>
        </div>

        {/* Receiver */}
        <div className={styles.detailSection}>
          <h3 className={styles.sectionLabel}>Receiver</h3>
          <div className={styles.detailCard}>
            <DetailRow label="Username" value={transaction.receiverName} />
            <DetailRow label="Account">
              <span className={styles.acctNumber}>{transaction.receiverAccountNumber}</span>
            </DetailRow>
          </div>
        </div>
      </div>

      {/* Transaction Info */}
      <div className={styles.infoCard}>
        <h3 className={styles.sectionLabel}>Transaction Details</h3>
        <div className={styles.infoGrid}>
          <DetailRow label="Currency" value={transaction.currency} />
          <DetailRow label="Status">
            <span className={styles.statusBadgeInline}>{transaction.status}</span>
          </DetailRow>
          <DetailRow label="Description" value={transaction.description || '—'} />
          <DetailRow label="Created At" value={formatDateTime(transaction.createdAt)} />
          {transaction.updatedAt && (
            <DetailRow label="Updated At" value={formatDateTime(transaction.updatedAt)} />
          )}
          <DetailRow label="Idempotency Key">
            <span className={styles.monoText}>{transaction.idempotencyKey}</span>
          </DetailRow>
        </div>

        {/* Phase 4 Note */}
        <div className={styles.phase4Note}>
          <span className={styles.phase4Label}>Risk Score & Fraud Decision</span>
          <span className={styles.phase4Value}>Pending analysis (Phase 4)</span>
        </div>
      </div>
    </div>
  );
};

const DetailRow = ({ label, value, children }) => (
  <div className={styles.detailRow}>
    <span className={styles.detailLabel}>{label}</span>
    <span className={styles.detailValue}>
      {children || value}
    </span>
  </div>
);

export default TransactionDetail;
