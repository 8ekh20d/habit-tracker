import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { verifyEmail } from '../api/auth';

const VerifyEmail: React.FC = () => {
  const [searchParams] = useSearchParams();
  const [message, setMessage] = useState('Verifying your email...');
  const [error, setError] = useState('');
  const [isVerified, setIsVerified] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const token = searchParams.get('token');
    
    if (!token) {
      setError('Invalid verification link');
      return;
    }

    const verify = async () => {
      try {
        const response = await verifyEmail(token);
        setMessage(response.message);
        setIsVerified(true);
      } catch (err: any) {
        setError(err.response?.data?.message || 'Verification failed');
      }
    };

    verify();
  }, [searchParams]);

  const handleContinue = () => {
    navigate('/login');
  };

  return (
    <div style={{ 
      maxWidth: '500px', 
      margin: '100px auto', 
      padding: '40px', 
      textAlign: 'center',
      border: '1px solid #ddd',
      borderRadius: '8px',
      boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
    }}>
      {error ? (
        <>
          <div style={{ fontSize: '48px', marginBottom: '20px' }}>❌</div>
          <h2 style={{ color: '#f44336', marginBottom: '20px' }}>Verification Failed</h2>
          <p style={{ color: '#666', marginBottom: '30px', fontSize: '16px' }}>{error}</p>
          <button
            onClick={handleContinue}
            style={{
              padding: '12px 30px',
              fontSize: '16px',
              backgroundColor: '#2196F3',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Go to Login
          </button>
        </>
      ) : isVerified ? (
        <>
          <div style={{ fontSize: '64px', marginBottom: '20px' }}>✅</div>
          <h2 style={{ color: '#4CAF50', marginBottom: '20px' }}>Email Verified Successfully!</h2>
          <p style={{ color: '#666', marginBottom: '30px', fontSize: '16px' }}>
            Your email has been verified. You can now log in to your account.
          </p>
          <button
            onClick={handleContinue}
            style={{
              padding: '12px 30px',
              fontSize: '16px',
              backgroundColor: '#4CAF50',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
            onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#45a049'}
            onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#4CAF50'}
          >
            Continue to Login
          </button>
        </>
      ) : (
        <>
          <div style={{ fontSize: '48px', marginBottom: '20px' }}>⏳</div>
          <h2 style={{ marginBottom: '20px' }}>Verifying Email</h2>
          <p style={{ color: '#666', fontSize: '16px' }}>{message}</p>
        </>
      )}
    </div>
  );
};

export default VerifyEmail;
