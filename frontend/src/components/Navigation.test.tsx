import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Navigation from './Navigation';

describe('Navigation Component', () => {
  const mockOnLogout = jest.fn();
  const testEmail = 'test@example.com';

  beforeEach(() => {
    mockOnLogout.mockClear();
  });

  test('renders navigation links', () => {
    render(
      <BrowserRouter>
        <Navigation userEmail={testEmail} onLogout={mockOnLogout} />
      </BrowserRouter>
    );

    expect(screen.getByText('Habits')).toBeInTheDocument();
    expect(screen.getByText('Statistics')).toBeInTheDocument();
  });

  test('displays user email', () => {
    render(
      <BrowserRouter>
        <Navigation userEmail={testEmail} onLogout={mockOnLogout} />
      </BrowserRouter>
    );

    expect(screen.getByText(testEmail)).toBeInTheDocument();
  });

  test('calls onLogout when logout button is clicked', () => {
    render(
      <BrowserRouter>
        <Navigation userEmail={testEmail} onLogout={mockOnLogout} />
      </BrowserRouter>
    );

    const logoutButton = screen.getByText('Logout');
    fireEvent.click(logoutButton);

    expect(mockOnLogout).toHaveBeenCalledTimes(1);
  });
});
