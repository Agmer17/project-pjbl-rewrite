
'use strict';

function handleCategoryChange(select) {
    const baseUrl = window.location.pathname; // path tanpa query
    const value = select.value;

    if (!value) {
        // kalau kosong, redirect ke path saja tanpa parameter
        window.location.href = baseUrl;
    } else {
        // kalau ada value, redirect dengan parameter cat
        window.location.href = baseUrl + '?cat=' + value;
    }
}

function openDeleteProductModal(button) {
    const userId = button.getAttribute('data-product-id');
    const username = button.getAttribute('data-product-name') || 'Pengguna Ini';
    const deleteUrl = '/admin/products/delete/' + userId;

    const modal = document.getElementById('delete_product_modal');

    if (modal) {
        document.getElementById('modal-product-name').textContent = username;
        document.getElementById('delete-product-link').href = deleteUrl;

        modal.showModal();
    } else {
        console.error('Modal tidak ditemukan!');
    }
}