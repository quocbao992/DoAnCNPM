import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';
import { formatService } from '../../services/formatService';
import { resolveProductImage, defaultCoffeeImg } from '../../images';

export const ProductCard = ({ product, onNavigate }) => {
  const { isAuthenticated } = useAuth();
  const { addToCart } = useCart();
  const [selectedSize, setSelectedSize] = useState('M');
  const [isAdded, setIsAdded] = useState(false);

  const sizeExtra = selectedSize === 'L' ? 10000 : selectedSize === 'M' ? 5000 : 0;
  const currentPrice = (product.unitPrice || 0) + sizeExtra;

  const handleAdd = () => {
    if (!isAuthenticated) {
      if (onNavigate) onNavigate('login');
      return;
    }
    addToCart(product, 1, selectedSize);
    setIsAdded(true);
    setTimeout(() => setIsAdded(false), 1200);
  };

  return (
    <div className="product-card">
      <div className="card-img-wrap">
        <img
          src={resolveProductImage(product)}
          alt={product.name}
          className="card-img"
          onError={(e) => {
            e.target.src = defaultCoffeeImg;
          }}
        />
        {product.categoryName && (
          <span className="card-badge">{product.categoryName}</span>
        )}
      </div>

      <h3 className="card-title">{product.name}</h3>

      <div style={{ display: 'flex', gap: '6px', margin: '8px 0 12px' }}>
        {['S', 'M', 'L'].map((sz) => (
          <button
            key={sz}
            onClick={() => setSelectedSize(sz)}
            style={{
              padding: '4px 10px',
              borderRadius: '6px',
              fontSize: '0.78rem',
              fontWeight: 700,
              border: '1px solid var(--color-border)',
              background:
                selectedSize === sz
                  ? 'var(--color-primary-dark)'
                  : 'var(--color-surface-muted)',
              color: selectedSize === sz ? '#fff' : 'var(--color-text-secondary)',
              cursor: 'pointer',
              transition: 'var(--transition)',
            }}
          >
            Size {sz}
          </button>
        ))}
      </div>

      <div className="card-meta">
        <div className="card-price">
          {formatService.formatMoney(currentPrice)}
        </div>
        <button
          className={`btn btn-sm ${isAdded ? 'btn-secondary' : 'btn-primary'}`}
          onClick={handleAdd}
          disabled={!product.status}
        >
          {isAdded ? 'Đã thêm' : 'Thêm vào giỏ'}
        </button>

      </div>
    </div>
  );
};

export default ProductCard;
