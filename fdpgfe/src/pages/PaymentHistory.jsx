import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Search, CreditCard, ArrowUpRight, ArrowDownLeft } from 'lucide-react';
import paymentService from '../services/paymentService';
import styles from './PaymentHistory.module.css';

const PaymentHistory = () => {
  const [transactions, setTransactions] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL'); // ALL | SENT | RECEIVED
  const [page, setPage] = useState(1);
  const PAGE_SIZE = 10;

  useEffect(() => {
    const fetchAll = async () => {
      try {
        const res = await paymentService.getUserPayments();
        if (res.data.success) {
          setTransactions(res.data.data);
          setFiltered(res.data.data);
        } else {
          setError(res.data.message);
        }
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load transactions');
      } finally {
        setLoading(false);
      }
    };
    fetchAll();
  }, []);

  const applyFilters = (list, q, type) => {
    let result = list;
    if (type !== 'ALL') result = result.filter(tx => tx.transactionType === type);
    if (q.trim()) {
      const lq = q.toLowerCase();
      result = result.filter(tx =>
        tx.transactionId.toLowerCase().includes(lq) ||
        (tx.counterpartyName && tx.counterpartyName.toLowerCase().includes(lq)) ||
        (tx.counterpartyAccountNumber && tx.counterpartyAccountNumber.toLowerCase().includes(lq))
      );
    }
    return result;
  };

  const handleSearch = (e) => {
    const q = e.target.value;
    setSearch(q);
    setPage(1);
    setFiltered(applyFilters(transactions, q, typeFilter));
  };

  const handleTypeFilter = (type) => {
    setTypeFilter(type);
    setPage(1);
    setFiltered(applyFilters(transactions, search, type));
  };

  const formatAmount = (amount, currency) => {
    const symbol = currency === 'INR' ? '₹' : '$';
    return `${symbol}${Number(amount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
  };

  const formatDate = (date) =>
    new Date(date).toLocaleString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
      hour: '2-digit', minute: '2-digit',
    });

  const sentCount = transactions.filter(t => t.transactionType === 'SENT').length;
  const receivedCount = transactions.filter(t => t.transactionType === 'RECEIVED').length;

  const totalPages = Math.ceil(filtered.length / PAGE_SIZE);
  const paginated = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  return (
    <div className={styles.container}>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.title}>Payment History</h1>
          <p className={styles.subtitle}>
            {transactions.length} total &nbsp;·&nbsp;
            <span className={styles.sentLabel}>↑ {sentCount} sent</span>
            &nbsp;·&nbsp;
            <span className={styles.receivedLabel}>↓ {receivedCount} received</span>
          </p>
        </div>
        <Link to="/payments/new" className={styles.newPaymentBtn} id="new-payment-btn">
          + New Payment
        </Link>
      </div>

      {/* Filter Tabs + Search */}
      <div className={styles.controlsRow}>
        <div className={styles.tabs}>
          {['ALL', 'SENT', 'RECEIVED'].map(t => (
            <button
              key={t}
              className={`${styles.tab} ${typeFilter === t ? styles.tabActive : ''}`}
              onClick={() => handleTypeFilter(t)}
              id={`filter-${t.toLowerCase()}`}
            >
              {t === 'SENT' && <ArrowUpRight size={13} />}
              {t === 'RECEIVED' && <ArrowDownLeft size={13} />}
              {t}
            </button>
          ))}
        </div>
        <div className={styles.searchWrapper}>
          <Search size={15} className={styles.searchIcon} />
          <input
            className={styles.searchInput}
            type="text"
            id="search-transactions"
            placeholder="Search by ID, name or account..."
            value={search}
            onChange={handleSearch}
          />
        </div>
      </div>

      {loading ? (
        <div className={styles.centered}>
          <div className={styles.spinner} />
          <p>Loading transactions...</p>
        </div>
      ) : error ? (
        <div className={styles.errorState}>{error}</div>
      ) : filtered.length === 0 ? (
        <div className={styles.emptyState}>
          <CreditCard size={48} color="var(--text-secondary)" />
          <h3>No transactions found</h3>
          <p>{search || typeFilter !== 'ALL' ? 'Try a different filter.' : 'Make your first payment to get started.'}</p>
          {!search && typeFilter === 'ALL' && (
            <Link to="/payments/new" className={styles.emptyLink}>Make a Payment →</Link>
          )}
        </div>
      ) : (
        <>
          <div className={styles.tableCard}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th className={styles.th}>Type</th>
                  <th className={styles.th}>Transaction ID</th>
                  <th className={styles.th}>Counterparty</th>
                  <th className={styles.th}>Account No.</th>
                  <th className={styles.th}>Amount</th>
                  <th className={styles.th}>Date</th>
                  <th className={styles.th}>Status</th>
                  <th className={styles.th}></th>
                </tr>
              </thead>
              <tbody>
                {paginated.map((tx) => {
                  const isSent = tx.transactionType === 'SENT';
                  return (
                    <tr key={tx.transactionId} className={styles.tr}>
                      {/* Direction badge */}
                      <td className={styles.td}>
                        <span className={`${styles.typeBadge} ${isSent ? styles.typeSent : styles.typeReceived}`}>
                          {isSent
                            ? <><ArrowUpRight size={11} /> Sent</>
                            : <><ArrowDownLeft size={11} /> Received</>}
                        </span>
                      </td>
                      <td className={styles.td}>
                        <span className={styles.txId} title={tx.transactionId}>
                          {tx.transactionId.slice(0, 8)}...
                        </span>
                      </td>
                      <td className={styles.td}>
                        <span className={styles.receiverName}>{tx.counterpartyName || '—'}</span>
                      </td>
                      <td className={styles.td}>
                        <span className={styles.accountNo}>{tx.counterpartyAccountNumber}</span>
                      </td>
                      <td className={styles.td}>
                        <span className={isSent ? styles.amountDebit : styles.amountCredit}>
                          {isSent ? '−' : '+'}{formatAmount(tx.amount, tx.currency)}
                        </span>
                      </td>
                      <td className={styles.td}>
                        <span className={styles.date}>{formatDate(tx.createdAt)}</span>
                      </td>
                      <td className={styles.td}>
                        <span className={styles.statusBadge}>{tx.status}</span>
                      </td>
                      <td className={styles.td}>
                        <Link
                          to={`/payments/${tx.transactionId}`}
                          className={styles.viewBtn}
                          id={`view-${tx.transactionId}`}
                        >
                          View
                        </Link>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className={styles.pagination}>
              <button
                className={styles.pageBtn}
                onClick={() => setPage((p) => Math.max(1, p - 1))}
                disabled={page === 1}
                id="prev-page-btn"
              >
                ← Prev
              </button>
              <span className={styles.pageInfo}>Page {page} of {totalPages}</span>
              <button
                className={styles.pageBtn}
                onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                disabled={page === totalPages}
                id="next-page-btn"
              >
                Next →
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default PaymentHistory;
