import React from 'react';
import '../styles/styles.css';

const MainContent = ({ children, title, actions }) => {
  return (
    <main className="main-content">
      {title && (
        <div className="content-header">
          <h2 className="content-title">{title}</h2>
          {actions && (
            <div className="content-actions">
              {actions}
            </div>
          )}
        </div>
      )}
      <div className="content-body">
        {children}
      </div>
    </main>
  );
};

export default MainContent;