const cart = [];

function formatCurrency(value) {
    return '₹' + Number(value).toFixed(2);
}

function getCartSummary() {
    let subtotal = 0;
    cart.forEach(item => {
        subtotal += Number(item.price) * Number(item.quantity);
    });
    const tax = subtotal * 0.10;
    const total = subtotal + tax;
    return { subtotal, tax, total };
}

function renderCart() {
    const cartItems = document.getElementById('cartItems');
    const subtotalDisplay = document.getElementById('subtotalDisplay');
    const taxDisplay = document.getElementById('taxDisplay');
    const totalDisplay = document.getElementById('totalDisplay');

    if (cart.length === 0) {
        cartItems.innerHTML = '<div class="text-muted">No medicines added yet.</div>';
        subtotalDisplay.textContent = formatCurrency(0);
        taxDisplay.textContent = formatCurrency(0);
        totalDisplay.textContent = formatCurrency(0);
        return;
    }

    let html = '';
    cart.forEach((item, index) => {
        html += `
            <div class="cart-item">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <strong>${item.name}</strong><br>
                        <small class="text-muted">₹${Number(item.price).toFixed(2)} each</small>
                    </div>
                    <div class="d-flex align-items-center gap-2">
                        <button type="button" class="btn btn-sm btn-outline-secondary" data-action="decrease" data-index="${index}">-</button>
                        <span>${item.quantity}</span>
                        <button type="button" class="btn btn-sm btn-outline-secondary" data-action="increase" data-index="${index}">+</button>
                        <button type="button" class="btn btn-sm btn-outline-danger" data-action="remove" data-index="${index}">Remove</button>
                    </div>
                </div>
            </div>
        `;
    });

    cartItems.innerHTML = html;

    const summary = getCartSummary();
    subtotalDisplay.textContent = formatCurrency(summary.subtotal);
    taxDisplay.textContent = formatCurrency(summary.tax);
    totalDisplay.textContent = formatCurrency(summary.total);

    const hiddenFields = [];
    cart.forEach(item => {
        hiddenFields.push(`<input type="hidden" name="medicineId[]" value="${item.id}">`);
        hiddenFields.push(`<input type="hidden" name="quantity[]" value="${item.quantity}">`);
    });

    const existingHidden = document.querySelectorAll('input[name="medicineId[]"], input[name="quantity[]"]');
    existingHidden.forEach(el => el.remove());
    document.querySelector('form').insertAdjacentHTML('beforeend', hiddenFields.join(''));
}

function addToCart(id, name, price, stock) {
    const existing = cart.find(item => item.id === id);
    if (existing) {
        if (existing.quantity >= stock) {
            alert('Requested quantity exceeds available stock.');
            return;
        }
        existing.quantity += 1;
    } else {
        cart.push({ id, name, price, quantity: 1, stock });
    }
    renderCart();
}

function adjustCart(index, delta) {
    const item = cart[index];
    if (!item) return;
    const newQty = item.quantity + delta;
    if (newQty <= 0) {
        cart.splice(index, 1);
    } else if (newQty > item.stock) {
        alert('Requested quantity exceeds available stock.');
        return;
    } else {
        item.quantity = newQty;
    }
    renderCart();
}

document.addEventListener('click', (event) => {
    const target = event.target;

    if (target.matches('.add-to-cart')) {
        addToCart(
            Number(target.dataset.id),
            target.dataset.name,
            Number(target.dataset.price),
            Number(target.dataset.stock)
        );
    }

    if (target.matches('[data-action="increase"]')) {
        adjustCart(Number(target.dataset.index), 1);
    }

    if (target.matches('[data-action="decrease"]')) {
        adjustCart(Number(target.dataset.index), -1);
    }

    if (target.matches('[data-action="remove"]')) {
        cart.splice(Number(target.dataset.index), 1);
        renderCart();
    }
});

document.getElementById('medicineSearch')?.addEventListener('input', async function () {
    const keyword = this.value.trim();
    try {
        const response = await fetch('/billing/search', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams({ keyword })
        });

        const medicines = await response.json();
        const tbody = document.getElementById('medicineTableBody');
        if (!tbody) return;

        tbody.innerHTML = medicines.map(medicine => `
            <tr>
                <td>${medicine.name}</td>
                <td>₹${Number(medicine.unitPrice).toFixed(2)}</td>
                <td>
                    <span class="${medicine.stockQuantity < 10 ? 'badge bg-warning text-dark' : 'badge bg-success'}">${medicine.stockQuantity}</span>
                </td>
                <td>
                    <span class="${medicine.expired ? 'badge badge-expired' : ''}">${medicine.expiryDate}</span>
                </td>
                <td>
                    <button type="button" class="btn btn-sm btn-primary add-to-cart"
                        data-id="${medicine.id}"
                        data-name="${medicine.name}"
                        data-price="${medicine.unitPrice}"
                        data-stock="${medicine.stockQuantity}">Add</button>
                </td>
            </tr>
        `).join('');
    } catch (error) {
        console.error('Search failed', error);
    }
});

renderCart();
