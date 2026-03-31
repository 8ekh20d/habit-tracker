import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { verifyEmail } from '../api/auth';

const VerifyEmail: React.FC = () => {
  const [searchParams] = useSearchParams();
  const [message, setMessage] = useState('Verifying your email...');
  const [error, setError] = useState('');
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
        setTimeout(() => navigate('/login'), 3000);
      } catch (err: any) {
        setError(err.response?.data?.message || 'Verification failed');
      }
    };

    verify();
  }, [searchParams, navigate]);

  return (
    <div style={{ maxWidth: '400px', margin: '50px auto', padding: '20px', textAlign: 'center' }}>
      <h2>Email Verification</h2>
      {error ? (
        <p style={{ color: 'red', marginTop: '20px' }}>{error}</p>
      ) : (
        <p style={{ color: 'green', marginTop: '20px' }}>{message}</p>
      )}
      <p style={{ marginTop: '20px' }}>
        <a href="/login">Go to Login</a>
      </p>
    </div>
  );
};

export default VerifyEmail;
