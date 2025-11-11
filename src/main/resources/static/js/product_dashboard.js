
'use strict';

// Handle Form Submit
const searchForm = document.getElementById("searchForm");
searchForm?.addEventListener("submit", function (e) {
    // Tidak perlu e.preventDefault() lagi!
    // Form akan submit secara natural dengan query params

    // Optional: reset page ke 0
    const url = new URL(window.location.href);
    url.searchParams.set("page", "0");

    // Ambil semua form data
    const formData = new FormData(searchForm);
    formData.forEach((value, key) => {
        if (value) {
            url.searchParams.set(key, value);
        } else {
            url.searchParams.delete(key);
        }
    });

    e.preventDefault();
    window.location.href = url.toString();
});

// Handle Category Change
const categorySelect = document.getElementById("categorySelect");
categorySelect?.addEventListener("change", function () {
    searchForm.requestSubmit();
});

// Handle Sort Change
const sortSelect = document.getElementById("sortSelect");
sortSelect?.addEventListener("change", function () {
    searchForm.requestSubmit();
});

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