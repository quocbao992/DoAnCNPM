import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';
import { formatService } from '../../services/formatService';
import { resolveProductImage, defaultCoffeeImg } from '../../images';
import { toppingApi } from '../../api';

const DEFAULT_TOPPINGS = [
  { id: 1, name: 'Trân Châu Trắng', unitPrice: 10000 },
  { id: 2, name: 'Thạch Cà Phê', unitPrice: 10000 },
  { id: 3, name: 'Kem Cheese Béo', unitPrice: 15000 },
  { id: 4, name: 'Sốt Caramel', unitPrice: 8000 },
];

export const ProductCard = ({ product, onNavigate }) => {
  const { isAuthenticated } = useAuth();
  const { addToCart } = useCart();
  const [selectedSize, setSelectedSize] = useState('M');
  const [availableToppings, setAvailableToppings] = useState(DEFAULT_TOPPINGS);
  const [selectedToppings, setSelectedToppings] = useState([]);
  const [showToppingPicker, setShowToppingPicker] = useState(false);
  const [isAdded, setIsAdded] = useState(false);

  useEffect(() => {
    let isMounted = true;
    toppingApi
      .getAll()
      .then((res) => {
        if (!isMounted) return;
        const list = Array.isArray(res) ? res : res?.data || [];
        if (list.length > 0) {
          setAvailableToppings(list);
        }
      })
      .catch(() => {});
    return () => {
      isMounted = false;
    };
  }, []);

  const sizeExtra = selectedSize === 'L' ? 10000 : selectedSize === 'M' ? 5000 : 0;
  const toppingsExtra = selectedToppings.reduce((sum, t) => sum + (Number(t.unitPrice) || 0), 0);
  const currentPrice = (product.unitPrice || 0) + sizeExtra + toppingsExtra;

  const handleToggleTopping = (topping) => {
    setSelectedToppings((prev) => {
      const exists = prev.some((t) => t.id === topping.id);
      if (exists) {
        return prev.filter((t) => t.id !== topping.id);
      }
      return [...prev, topping];
    });
  };

  const handleChooseNoTopping = () => {
    setSelectedToppings([]);
  };

  const handleAdd = () => {
    if (!isAuthenticated) {
      if (onNavigate) onNavigate('login');
      return;
    }
    addToCart(product, 1, selectedSize, '', selectedToppings);
    setIsAdded(true);
    setTimeout(() => setIsAdded(false), 1200);
  };

  return (
    <div className="product-card" style={{ display: 'flex', flexDirection: 'column' }}>
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

      <h3 className="card-title" style={{ fontWeight: 600 }}>{product.name}</h3>

      {/* Chọn Size */}
      <div style={{ display: 'flex', gap: '6px', margin: '8px 0 6px' }}>
        {['S', 'M', 'L'].map((sz) => (
          <button
            key={sz}
            onClick={() => setSelectedSize(sz)}
            style={{
              padding: '4px 10px',
              borderRadius: '6px',
              fontSize: '0.8rem',
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

      {/* Lựa chọn Topping */}
      <div style={{ margin: '4px 0 10px' }}>
        <button
          type="button"
          onClick={() => setShowToppingPicker(!showToppingPicker)}
          style={{
            fontSize: '0.8rem',
            color: 'var(--color-text-secondary)',
            textDecoration: 'underline',
            cursor: 'pointer',
            padding: '2px 0',
            background: 'none',
            border: 'none',
          }}
        >
          {showToppingPicker
            ? 'Đóng tùy chọn topping'
            : selectedToppings.length > 0
            ? `Topping: ${selectedToppings.map((t) => t.name).join(', ')} (sửa)`
            : '+ Thêm topping / Không lấy topping'}
        </button>

        {showToppingPicker && (
          <div
            style={{
              marginTop: '6px',
              padding: '8px',
              background: 'var(--color-surface-muted)',
              borderRadius: '8px',
              border: '1px solid var(--color-border)',
              fontSize: '0.78rem',
            }}
          >
            <div style={{ marginBottom: '6px' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={selectedToppings.length === 0}
                  onChange={handleChooseNoTopping}
                />
                <span>Không chọn topping</span>
              </label>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              {availableToppings.map((tp) => {
                const isChecked = selectedToppings.some((t) => t.id === tp.id);
                return (
                  <label
                    key={tp.id}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      cursor: 'pointer',
                    }}
                  >
                    <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <input
                        type="checkbox"
                        checked={isChecked}
                        onChange={() => handleToggleTopping(tp)}
                      />
                      {tp.name}
                    </span>
                    <span style={{ color: 'var(--color-text-secondary)' }}>
                      +{formatService.formatCurrency(tp.unitPrice || 0)}
                    </span>
                  </label>
                );
              })}
            </div>
          </div>
        )}
      </div>

      <div className="card-meta" style={{ marginTop: 'auto' }}>
        <div className="card-price" style={{ color: 'var(--color-text-main)' }}>
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
