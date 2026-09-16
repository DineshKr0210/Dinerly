# Dinerly Frontend - Code Examples
## Practical Implementation Snippets for React/JavaScript

---

## Table of Contents
1. [Setup & Configuration](#setup--configuration)
2. [Authentication Examples](#authentication-examples)
3. [Offers Implementation](#offers-implementation)
4. [Rewards Implementation](#rewards-implementation)
5. [Points Implementation](#points-implementation)

---

## Setup & Configuration

### API Client Setup

#### Using Axios
```javascript
import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

// Add JWT token to all requests
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwtToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Handle token expiry
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('jwtToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

#### Using Fetch API
```javascript
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

async function apiCall(endpoint, options = {}) {
  const token = localStorage.getItem('jwtToken');
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };
  
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });

    if (response.status === 401) {
      localStorage.removeItem('jwtToken');
      window.location.href = '/login';
    }

    const data = await response.json();
    
    if (!response.ok) {
      throw new Error(data.message || 'API Error');
    }

    return data;
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
}

export { apiCall };
```

---

## Authentication Examples

### Login Service
```javascript
// authService.js
import apiClient from './apiClient';

export const authService = {
  login: async (email, password) => {
    try {
      const response = await apiClient.post('/api/auth/login', {
        email,
        password,
      });
      
      if (response.data.success) {
        localStorage.setItem('jwtToken', response.data.data.token);
        localStorage.setItem('user', JSON.stringify(response.data.data.user));
        return response.data.data;
      }
      throw new Error(response.data.message);
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  },

  register: async (email, password, fullName) => {
    const response = await apiClient.post('/api/auth/register', {
      email,
      password,
      fullName,
    });
    return response.data;
  },

  logout: () => {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('user');
    localStorage.removeItem('offersCache');
    localStorage.removeItem('rewardsProfile');
  },

  getCurrentUser: () => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },
};
```

### Login Component
```javascript
// LoginPage.jsx
import React, { useState } from 'react';
import { authService } from '../services/authService';
import { useNavigate } from 'react-router-dom';

function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const user = await authService.login(email, password);
      
      // Redirect based on role
      if (user.role === 'ADMIN') {
        navigate('/admin/dashboard');
      } else if (user.role === 'GUEST') {
        navigate('/guest/home');
      } else if (user.role === 'STAFF') {
        navigate('/staff/dashboard');
      }
    } catch (err) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleLogin}>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="Email"
        required
      />
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="Password"
        required
      />
      {error && <div className="error">{error}</div>}
      <button type="submit" disabled={loading}>
        {loading ? 'Logging in...' : 'Login'}
      </button>
    </form>
  );
}

export default LoginPage;
```

---

## Offers Implementation

### Offers Service
```javascript
// offerService.js
import apiClient from './apiClient';

export const offerService = {
  // ADMIN: Create offer
  createOffer: async (offerData) => {
    const response = await apiClient.post('/api/admin/offers', offerData);
    return response.data.data;
  },

  // ADMIN: List offers
  listOffers: async (filters = {}, page = 0, size = 20) => {
    const params = new URLSearchParams({
      ...filters,
      page,
      size,
    });
    const response = await apiClient.get(`/api/admin/offers?${params}`);
    return response.data.data;
  },

  // ADMIN: Get offer details
  getOfferById: async (offerId) => {
    const response = await apiClient.get(`/api/admin/offers/${offerId}`);
    return response.data.data;
  },

  // ADMIN: Update offer
  updateOffer: async (offerId, offerData) => {
    const response = await apiClient.put(`/api/admin/offers/${offerId}`, offerData);
    return response.data.data;
  },

  // ADMIN: Delete offer
  deleteOffer: async (offerId) => {
    await apiClient.delete(`/api/admin/offers/${offerId}`);
  },

  // ADMIN: Toggle status
  toggleStatus: async (offerId) => {
    const response = await apiClient.put(`/api/admin/offers/${offerId}/toggle-status`);
    return response.data.data;
  },

  // GUEST: List available offers
  listGuestOffers: async (filters = {}, page = 0, size = 20) => {
    const params = new URLSearchParams({
      ...filters,
      page,
      size,
    });
    const response = await apiClient.get(`/api/offers?${params}`);
    return response.data.data;
  },

  // GUEST: Get offer details
  getGuestOffer: async (offerId) => {
    const response = await apiClient.get(`/api/offers/${offerId}`);
    return response.data.data;
  },

  // GUEST: Redeem offer
  redeemOffer: async (offerId, locationId) => {
    const response = await apiClient.post(
      `/api/offers/${offerId}/redeem?locationId=${locationId}`
    );
    return response.data.data;
  },

  // STAFF: Confirm redemption code
  confirmRedemptionCode: async (code) => {
    const response = await apiClient.post(`/api/offers/redeem/${code}/confirm`);
    return response.data.data;
  },
};
```

### Browse Offers Component
```javascript
// OffersList.jsx
import React, { useState, useEffect } from 'react';
import { offerService } from '../services/offerService';

function OffersList({ locationId }) {
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [error, setError] = useState('');

  useEffect(() => {
    loadOffers();
  }, [page, locationId]);

  const loadOffers = async () => {
    setLoading(true);
    try {
      const data = await offerService.listGuestOffers(
        { locationId },
        page,
        20
      );
      setOffers(data.content);
      setTotalPages(data.totalPages);
      setError('');
    } catch (err) {
      setError(err.message || 'Failed to load offers');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="offers-container">
      <h1>Available Offers</h1>
      
      {loading && <div className="loading">Loading offers...</div>}
      {error && <div className="error">{error}</div>}

      <div className="offers-grid">
        {offers.map((offer) => (
          <OfferCard key={offer.id} offer={offer} />
        ))}
      </div>

      <div className="pagination">
        <button 
          onClick={() => setPage(p => p - 1)} 
          disabled={page === 0}
        >
          Previous
        </button>
        <span>Page {page + 1} of {totalPages}</span>
        <button 
          onClick={() => setPage(p => p + 1)} 
          disabled={page + 1 >= totalPages}
        >
          Next
        </button>
      </div>
    </div>
  );
}

function OfferCard({ offer }) {
  const handleRedeem = async () => {
    try {
      const result = await offerService.redeemOffer(offer.id, offer.restaurantId);
      alert(`Redemption code: ${result.redemptionCode}`);
    } catch (error) {
      alert(`Error: ${error.message}`);
    }
  };

  return (
    <div className="offer-card">
      {offer.photoUrl && (
        <img src={offer.photoUrl} alt={offer.name} />
      )}
      <h3>{offer.name}</h3>
      <p className="discount">{offer.discountLabel}</p>
      <p className="description">{offer.description}</p>
      <p className="validity">
        Valid: {offer.startDate} to {offer.endDate}
      </p>
      
      {offer.redeemable ? (
        <button onClick={handleRedeem} className="btn-redeem">
          Redeem Now
        </button>
      ) : (
        <div className="not-redeemable">
          {offer.reasonIfNotRedeemable || 'Not available'}
        </div>
      )}

      <p className="redemptions">
        You've used this {offer.userRedemptionsTotal} time(s)
      </p>
    </div>
  );
}

export default OffersList;
```

### Confirm Code Component (Staff)
```javascript
// RedemptionValidator.jsx
import React, { useState } from 'react';
import { offerService } from '../services/offerService';

function RedemptionValidator() {
  const [code, setCode] = useState('');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleValidate = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const data = await offerService.confirmRedemptionCode(code);
      setResult(data);
    } catch (err) {
      setError(err.message || 'Invalid code');
      setResult(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="validator">
      <h2>Validate Redemption Code</h2>
      
      <form onSubmit={handleValidate}>
        <input
          type="text"
          value={code}
          onChange={(e) => setCode(e.target.value.toUpperCase())}
          placeholder="Enter redemption code"
          required
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Validating...' : 'Validate'}
        </button>
      </form>

      {error && <div className="error">{error}</div>}

      {result && (
        <div className="result success">
          <h3>✓ Code Valid</h3>
          <p><strong>Offer:</strong> {result.offerName}</p>
          <p><strong>Customer:</strong> {result.userEmail}</p>
          <p><strong>Phone:</strong> {result.mobileNumber}</p>
          <p><strong>Discount:</strong> {result.discountLabel}</p>
          <p><strong>Value:</strong> ${result.discountValue}</p>
          <p className="expires">
            Code expires: {new Date(result.codeExpiresAt).toLocaleString()}
          </p>
        </div>
      )}
    </div>
  );
}

export default RedemptionValidator;
```

---

## Rewards Implementation

### Rewards Service
```javascript
// rewardsService.js
import apiClient from './apiClient';

export const rewardsService = {
  // ADMIN: Create tier
  createTier: async (tierData) => {
    const response = await apiClient.post('/api/admin/rewards/tiers', tierData);
    return response.data.data;
  },

  // ADMIN: List tiers
  listTiers: async (page = 0, size = 20) => {
    const response = await apiClient.get(`/api/admin/rewards/tiers?page=${page}&size=${size}`);
    return response.data.data;
  },

  // ADMIN: Create reward item
  createRewardItem: async (itemData) => {
    const response = await apiClient.post('/api/admin/reward-items', itemData);
    return response.data.data;
  },

  // ADMIN: List reward items
  listRewardItems: async (filters = {}, page = 0, size = 20) => {
    const params = new URLSearchParams({
      ...filters,
      page,
      size,
    });
    const response = await apiClient.get(`/api/admin/reward-items?${params}`);
    return response.data.data;
  },

  // GUEST: Get rewards profile
  getRewardsProfile: async (restaurantId = 1) => {
    const response = await apiClient.get(`/api/rewards/profile?restaurantId=${restaurantId}`);
    return response.data.data;
  },

  // GUEST: Redeem reward
  redeemReward: async (rewardId, restaurantId) => {
    const response = await apiClient.post(
      `/api/rewards/${rewardId}/redeem`,
      { restaurantId }
    );
    return response.data.data;
  },

  // GUEST: Claim receipt
  claimReceipt: async (file, restaurantId, amount, date) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('restaurantId', restaurantId);
    if (amount) formData.append('receiptAmount', amount);
    if (date) formData.append('receiptDate', date);

    const response = await apiClient.post('/api/rewards/receipt/claim', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data.data;
  },
};
```

### Rewards Profile Component
```javascript
// RewardsProfile.jsx
import React, { useState, useEffect } from 'react';
import { rewardsService } from '../services/rewardsService';

function RewardsProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      const data = await rewardsService.getRewardsProfile();
      setProfile(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div className="error">{error}</div>;
  if (!profile) return <div>No profile data</div>;

  return (
    <div className="rewards-profile">
      <div className="header">
        <h1>My Rewards</h1>
        <div className="points-display">
          <span className="points-value">{profile.currentPoints}</span>
          <span className="points-label">Points</span>
        </div>
      </div>

      {/* Current Tier */}
      <div className="tier-section">
        <h3>Current Tier: {profile.currentTier.name}</h3>
        <div className="tier-badge">
          <img 
            src={`/images/tier-${profile.currentTier.name.toLowerCase()}.png`}
            alt={profile.currentTier.name}
          />
        </div>
        <ul className="benefits">
          {profile.currentTier.benefits.map((benefit, idx) => (
            <li key={idx}>✓ {benefit}</li>
          ))}
        </ul>
      </div>

      {/* Next Tier Progress */}
      {profile.nextTier && (
        <div className="next-tier-section">
          <h4>Next Tier: {profile.nextTier.name}</h4>
          <div className="progress-bar">
            <div 
              className="progress"
              style={{
                width: `${(profile.currentPoints / profile.nextTier.minPoints) * 100}%`
              }}
            />
          </div>
          <p>
            {profile.nextTier.pointsUntilNextTier} points to {profile.nextTier.name}
          </p>
        </div>
      )}

      {/* Available Rewards */}
      <div className="available-rewards">
        <h3>Available Rewards</h3>
        <div className="rewards-grid">
          {profile.availableRewards.map((reward) => (
            <RewardItem 
              key={reward.id} 
              reward={reward}
              onRedeem={loadProfile}
            />
          ))}
        </div>
      </div>
    </div>
  );
}

function RewardItem({ reward, onRedeem }) {
  const [redeeming, setRedeeming] = useState(false);

  const handleRedeem = async () => {
    setRedeeming(true);
    try {
      const result = await rewardsService.redeemReward(reward.id, 1);
      alert(`Reward redeemed! Code: ${result.redemptionCode}`);
      onRedeem();
    } catch (error) {
      alert(`Error: ${error.message}`);
    } finally {
      setRedeeming(false);
    }
  };

  return (
    <div className="reward-item">
      <h4>{reward.name}</h4>
      <p className="description">{reward.description}</p>
      <p className="points-required">
        {reward.pointsRequired} points
      </p>
      <button
        onClick={handleRedeem}
        disabled={!reward.canRedeem || redeeming}
        className="btn-redeem"
      >
        {redeeming ? 'Redeeming...' : 'Redeem Now'}
      </button>
    </div>
  );
}

export default RewardsProfile;
```

### Claim Receipt Component
```javascript
// ClaimReceiptForm.jsx
import React, { useState } from 'react';
import { rewardsService } from '../services/rewardsService';

function ClaimReceiptForm() {
  const [file, setFile] = useState(null);
  const [amount, setAmount] = useState('');
  const [date, setDate] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!file) {
      setError('Please select a receipt image');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const data = await rewardsService.claimReceipt(
        file,
        1, // restaurantId
        amount,
        date
      );
      setResult(data);
      setFile(null);
      setAmount('');
      setDate('');
    } catch (err) {
      setError(err.message || 'Failed to claim receipt');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="claim-receipt">
      <h2>Claim Points from Receipt</h2>
      
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Upload Receipt Photo</label>
          <input
            type="file"
            accept="image/*"
            onChange={(e) => setFile(e.target.files[0])}
            required
          />
        </div>

        <div className="form-group">
          <label>Amount (Optional)</label>
          <input
            type="number"
            step="0.01"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            placeholder="$0.00"
          />
        </div>

        <div className="form-group">
          <label>Date (Optional)</label>
          <input
            type="date"
            value={date}
            onChange={(e) => setDate(e.target.value)}
          />
        </div>

        <button type="submit" disabled={loading}>
          {loading ? 'Processing...' : 'Claim Points'}
        </button>
      </form>

      {error && <div className="error">{error}</div>}
      
      {result && (
        <div className="result success">
          <h3>✓ Receipt Submitted</h3>
          <p>Points awarded: {result.pointsAwarded}</p>
          <p>New balance: {result.newBalance}</p>
          <p>Status: {result.status}</p>
        </div>
      )}
    </div>
  );
}

export default ClaimReceiptForm;
```

---

## Points Implementation

### Points Service
```javascript
// pointsService.js
import apiClient from './apiClient';

export const pointsService = {
  // ADMIN: Credit points
  creditPoints: async (userId, amount, reason) => {
    const response = await apiClient.post('/api/admin/points/credit', {
      userId,
      amount,
      reason,
    });
    return response.data.data;
  },

  // ADMIN: Debit points
  debitPoints: async (userId, amount, reason) => {
    const response = await apiClient.post('/api/admin/points/debit', {
      userId,
      amount,
      reason,
    });
    return response.data.data;
  },

  // ADMIN: Bulk credit
  bulkCredit: async (userIds, amount, reason) => {
    const response = await apiClient.post('/api/admin/points/bulk-credit', {
      userIds,
      amount,
      reason,
    });
    return response.data.data;
  },

  // ADMIN: Get balance
  getBalance: async (userId) => {
    const response = await apiClient.get(`/api/admin/points/balance/${userId}`);
    return response.data.data;
  },

  // ADMIN: Get ledger
  getLedger: async (userId, page = 0, size = 20) => {
    const response = await apiClient.get(
      `/api/admin/points/ledger/${userId}?page=${page}&size=${size}`
    );
    return response.data.data;
  },

  // ADMIN: Get statistics
  getStatistics: async (restaurantId) => {
    const response = await apiClient.get(
      `/api/admin/points/statistics?restaurantId=${restaurantId}`
    );
    return response.data.data;
  },

  // ADMIN: Get top earners
  getTopEarners: async (restaurantId, limit = 10) => {
    const response = await apiClient.get(
      `/api/admin/points/top-earners?restaurantId=${restaurantId}&limit=${limit}`
    );
    return response.data.data;
  },
};
```

### Admin Points Management Component
```javascript
// AdminPointsManager.jsx
import React, { useState } from 'react';
import { pointsService } from '../services/pointsService';

function AdminPointsManager() {
  const [userId, setUserId] = useState('');
  const [amount, setAmount] = useState('');
  const [reason, setReason] = useState('');
  const [action, setAction] = useState('credit');
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const data = action === 'credit'
        ? await pointsService.creditPoints(userId, amount, reason)
        : await pointsService.debitPoints(userId, amount, reason);

      setResult(data);
      setUserId('');
      setAmount('');
      setReason('');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="points-manager">
      <h2>Manage Guest Points</h2>

      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Action</label>
          <select value={action} onChange={(e) => setAction(e.target.value)}>
            <option value="credit">Credit Points</option>
            <option value="debit">Debit Points</option>
          </select>
        </div>

        <div className="form-group">
          <label>User ID</label>
          <input
            type="number"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            placeholder="Enter user ID"
            required
          />
        </div>

        <div className="form-group">
          <label>Amount</label>
          <input
            type="number"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            placeholder="Enter amount"
            required
          />
        </div>

        <div className="form-group">
          <label>Reason</label>
          <input
            type="text"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="e.g., 'Restaurant visit'"
            required
          />
        </div>

        <button type="submit" disabled={loading}>
          {loading ? 'Processing...' : 'Submit'}
        </button>
      </form>

      {error && <div className="error">{error}</div>}

      {result && (
        <div className="result success">
          <h3>✓ Operation Successful</h3>
          <p><strong>User ID:</strong> {result.userId}</p>
          <p><strong>Points {action === 'credit' ? 'Added' : 'Deducted'}:</strong> {result.pointsAdded || result.pointsDeducted}</p>
          <p><strong>New Balance:</strong> {result.newBalance}</p>
        </div>
      )}
    </div>
  );
}

export default AdminPointsManager;
```

### Points Ledger Component
```javascript
// PointsLedger.jsx
import React, { useState, useEffect } from 'react';
import { pointsService } from '../services/pointsService';

function PointsLedger({ userId }) {
  const [ledger, setLedger] = useState([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadLedger();
  }, [userId, page]);

  const loadLedger = async () => {
    setLoading(true);
    try {
      const data = await pointsService.getLedger(userId, page, 20);
      setLedger(data.content || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div className="error">{error}</div>;

  return (
    <div className="points-ledger">
      <h3>Points Transaction History</h3>
      
      <table>
        <thead>
          <tr>
            <th>Date</th>
            <th>Type</th>
            <th>Amount</th>
            <th>Reason</th>
            <th>Balance</th>
          </tr>
        </thead>
        <tbody>
          {ledger.map((transaction, idx) => (
            <tr key={idx} className={transaction.type === 'CREDIT' ? 'credit' : 'debit'}>
              <td>{new Date(transaction.date).toLocaleString()}</td>
              <td>{transaction.type}</td>
              <td>{transaction.type === 'CREDIT' ? '+' : '-'}{transaction.amount}</td>
              <td>{transaction.reason}</td>
              <td><strong>{transaction.balance}</strong></td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="pagination">
        <button onClick={() => setPage(p => p - 1)} disabled={page === 0}>
          Previous
        </button>
        <button onClick={() => setPage(p => p + 1)}>
          Next
        </button>
      </div>
    </div>
  );
}

export default PointsLedger;
```

---

## Custom Hooks for Common Operations

### useOffers Hook
```javascript
// hooks/useOffers.js
import { useState, useEffect } from 'react';
import { offerService } from '../services/offerService';

export function useOffers(locationId, isAdmin = false) {
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadOffers();
  }, [page, locationId]);

  const loadOffers = async () => {
    setLoading(true);
    try {
      const service = isAdmin ? offerService.listOffers : offerService.listGuestOffers;
      const data = await service({ locationId }, page, 20);
      setOffers(data.content);
      setTotalPages(data.totalPages);
      setError('');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return {
    offers,
    loading,
    error,
    page,
    setPage,
    totalPages,
    refresh: loadOffers,
  };
}
```

### useRewardsProfile Hook
```javascript
// hooks/useRewardsProfile.js
import { useState, useEffect } from 'react';
import { rewardsService } from '../services/rewardsService';

export function useRewardsProfile(restaurantId = 1) {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadProfile();
  }, [restaurantId]);

  const loadProfile = async () => {
    setLoading(true);
    try {
      const data = await rewardsService.getRewardsProfile(restaurantId);
      setProfile(data);
      setError('');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return {
    profile,
    loading,
    error,
    refresh: loadProfile,
  };
}
```

---

## Error Handling Utils

```javascript
// utils/errorHandler.js
export function parseApiError(error) {
  if (error.response?.data?.data?.errors) {
    // Validation errors
    return error.response.data.data.errors
      .map(e => `${e.field}: ${e.message}`)
      .join('\n');
  }

  if (error.response?.data?.message) {
    return error.response.data.message;
  }

  if (error.message === 'Network Error') {
    return 'Network error. Please check your connection.';
  }

  return error.message || 'An unexpected error occurred';
}

export function isAuthError(error) {
  return error.response?.status === 401 || error.response?.status === 403;
}

export function isNotFoundError(error) {
  return error.response?.status === 404;
}

export function isValidationError(error) {
  return error.response?.status === 422;
}
```

---

**Last Updated**: 2026-09-16
**Framework**: React 18+
**Status**: Ready for Implementation ✅
