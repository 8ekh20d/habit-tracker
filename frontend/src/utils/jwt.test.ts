import { decodeJWT, getEmailFromToken } from './jwt';

describe('JWT Utilities', () => {
  // Sample JWT token with payload: { "sub": "test@example.com", "exp": 1234567890 }
  const validToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiZXhwIjoxMjM0NTY3ODkwfQ.signature';

  describe('decodeJWT', () => {
    test('decodes valid JWT token', () => {
      const decoded = decodeJWT(validToken);
      expect(decoded).toBeDefined();
      expect(decoded.sub).toBe('test@example.com');
      expect(decoded.exp).toBe(1234567890);
    });

    test('returns null for invalid token', () => {
      const decoded = decodeJWT('invalid.token');
      expect(decoded).toBeNull();
    });

    test('returns null for empty token', () => {
      const decoded = decodeJWT('');
      expect(decoded).toBeNull();
    });
  });

  describe('getEmailFromToken', () => {
    test('extracts email from valid token', () => {
      const email = getEmailFromToken(validToken);
      expect(email).toBe('test@example.com');
    });

    test('returns empty string for null token', () => {
      const email = getEmailFromToken(null);
      expect(email).toBe('');
    });

    test('returns empty string for invalid token', () => {
      const email = getEmailFromToken('invalid.token');
      expect(email).toBe('');
    });
  });
});
