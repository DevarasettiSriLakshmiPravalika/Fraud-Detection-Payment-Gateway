import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Send, Loader2, CheckCircle, Wallet, AlertCircle } from 'lucide-react';
import paymentService from '../services/paymentService';
import styles from './MakePayment.module.css';

const MakePayment = () => {
  const navigate = useNavigate();
  const [myAccount, setMyAccount] = useState(null);
  const [formData, setFormData] = useState({
    receiverAccountNumber: '',
    amount: '',
    description: '',
    idempotencyKey: '',
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(null);
  const [apiError, setApiError] = useState(null);

  useEffect(() => {
    paymentService.getMyAccount()
      .then(res => { if (res.data.success) setMyAccount(res.data.data); })
      .catch(() => {});
  }, []);

  const formatBalance = (balance, currency) => {
    const symbol = currency === 'INR' ? '₹' : '$';
    return `${symbol}${Number(balance).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
  };

  const validate = () => {
    const errs = {};
    if (!formData.receiverAccountNumber.trim())
      errs.receiverAccountNumber = 'Receiver account number is required';
    if (!formData.amount) {
      errs.amount = 'Amount is required';
    } else if (isNaN(formData.amount) || parseFloat(formData.amount) <= 0) {
      errs.amount = 'Amount must be greater than zero';
    } else if (myAccount && parseFloat(formData.amount) > parseFloat(myAccount.balance)) {
      errs.amount = 'Amount exceeds your available balance';
    }
    return errs;
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setErrors({ ...errors, [e.target.name]: undefined });
    setApiError(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);
    try {
      const payload = {
        receiverAccountNumber: formData.receiverAccountNumber.trim(),
        amount: parseFloat(formData.amount),
        description: formData.description.trim() || undefined,
        idempotencyKey: formData.idempotencyKey.trim() || undefined,
      };

      const response = await paymentService.createPayment(payload);
      if (response.data.success) {
        setSuccess(response.data.data);
        // Refresh balance
        paymentService.getMyAccount()
          .then(res => { if (res.data.success) setMyAccount(res.data.data); })
          .catch(() => {});
      } else {
        setApiError(response.data.message || 'Payment failed');
      }
    } catch (err) {
      const msg =
        err.response?.data?.errors?.[0] ||
        err.response?.data?.message ||
        'Failed to create payment';
      setApiError(msg);
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className={styles.container}>
        <div className={styles.successCard}>
          <CheckCircle size={56} color="var(--accent)" />
          <h2 className={styles.successTitle}>Payment Successful!</h2>
          <p className={styles.successSub}>Your transaction has been submitted and is pending processing.</p>

          <div className={styles.successDetails}>
            <DetailRow label="Transaction ID" value={success.transactionId} mono />
            <DetailRow label="From Account" value={success.senderAccountNumber} />
            <DetailRow label="To Account" value={success.receiverAccountNumber} />
            <DetailRow label="Receiver" value={success.receiverName} />
            <DetailRow
              label="Amount"
              value={`₹${Number(success.amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`}
            />
            <DetailRow label="Status">
              <span className={styles.statusBadge}>{success.status}</span>
            </DetailRow>
          </div>

          <div className={styles.successActions}>
            <button
              className={styles.btnSecondary}
              onClick={() => navigate('/payments')}
              id="view-history-btn"
            >
              View History
            </button>
            <button
              className={styles.btnPrimary}
              onClick={() => {
                setSuccess(null);
                setFormData({ receiverAccountNumber: '', amount: '', description: '', idempotencyKey: '' });
              }}
              id="make-another-btn"
            >
              Make Another
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>Make a Payment</h1>
        <p className={styles.subtitle}>Transfer money using the recipient's FDPG account number.</p>
      </div>

      {/* Balance Strip */}
      {myAccount && (
        <div className={styles.balanceStrip} id="sender-balance-strip">
          <div className={styles.balanceStripLeft}>
            <Wallet size={18} color="var(--accent)" />
            <span className={styles.balanceStripLabel}>Your Account</span>
            <span className={styles.balanceStripAccount}>{myAccount.accountNumber}</span>
          </div>
          <div className={styles.balanceStripRight}>
            <span className={styles.balanceStripLabel}>Available Balance</span>
            <span className={styles.balanceStripAmount}>
              {formatBalance(myAccount.balance, myAccount.currency)}
            </span>
          </div>
        </div>
      )}

      <div className={styles.card}>
        {apiError && (
          <div className={styles.errorBanner} role="alert">
            <AlertCircle size={16} />
            {apiError}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate>
          {/* Receiver Account Number */}
          <div className={styles.formGroup}>
            <label className={styles.label} htmlFor="receiverAccountNumber">
              Receiver Account Number *
            </label>
            <input
              className={`${styles.input} ${errors.receiverAccountNumber ? styles.inputError : ''}`}
              type="text"
              id="receiverAccountNumber"
              name="receiverAccountNumber"
              placeholder="e.g. FDPG100002"
              value={formData.receiverAccountNumber}
              onChange={handleChange}
              disabled={loading}
            />
            {errors.receiverAccountNumber && (
              <p className={styles.fieldError}>{errors.receiverAccountNumber}</p>
            )}
            <p className={styles.fieldHint}>Ask the recipient for their FDPG account number</p>
          </div>

          {/* Amount */}
          <div className={styles.formGroup}>
            <label className={styles.label} htmlFor="amount">Amount (₹) *</label>
            <div className={styles.amountInputWrapper}>
              <span className={styles.currencyPrefix}>₹</span>
              <input
                className={`${styles.inputAmount} ${errors.amount ? styles.inputError : ''}`}
                type="number"
                id="amount"
                name="amount"
                placeholder="0.00"
                min="0.01"
                step="0.01"
                value={formData.amount}
                onChange={handleChange}
                disabled={loading}
              />
            </div>
            {errors.amount && <p className={styles.fieldError}>{errors.amount}</p>}
            {myAccount && formData.amount && !errors.amount && (
              <p className={styles.fieldHint}>
                Remaining balance after transfer:{' '}
                <strong>
                  {formatBalance(
                    Math.max(0, parseFloat(myAccount.balance) - parseFloat(formData.amount || 0)),
                    myAccount.currency
                  )}
                </strong>
              </p>
            )}
          </div>

          {/* Description */}
          <div className={styles.formGroup}>
            <label className={styles.label} htmlFor="description">
              Description <span className={styles.optional}>(optional)</span>
            </label>
            <textarea
              className={styles.textarea}
              id="description"
              name="description"
              placeholder="What is this payment for?"
              value={formData.description}
              onChange={handleChange}
              disabled={loading}
              rows={3}
            />
          </div>

          {/* Idempotency Key */}
          <div className={styles.formGroup}>
            <label className={styles.label} htmlFor="idempotencyKey">
              Idempotency Key <span className={styles.optional}>(auto-generated if blank)</span>
            </label>
            <input
              className={styles.input}
              type="text"
              id="idempotencyKey"
              name="idempotencyKey"
              placeholder="Unique key to prevent duplicate submissions"
              value={formData.idempotencyKey}
              onChange={handleChange}
              disabled={loading}
            />
          </div>

          <button
            className={styles.submitBtn}
            type="submit"
            disabled={loading}
            id="submit-payment-btn"
          >
            {loading ? (
              <>
                <Loader2 className={styles.spinner} size={18} />
                Processing...
              </>
            ) : (
              <>
                <Send size={18} />
                Submit Payment
              </>
            )}
          </button>
        </form>
      </div>
    </div>
  );
};

const DetailRow = ({ label, value, mono, children }) => (
  <div style={{
    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
    padding: '0.65rem 0', borderBottom: '1px solid var(--border-color)'
  }}>
    <span style={{ fontSize: '0.83rem', color: 'var(--text-secondary)' }}>{label}</span>
    {children || (
      <span style={{
        fontSize: '0.875rem', fontWeight: 600,
        fontFamily: mono ? 'monospace' : 'inherit',
        wordBreak: 'break-all', textAlign: 'right', maxWidth: '60%'
      }}>
        {value}
      </span>
    )}
  </div>
);

export default MakePayment;
