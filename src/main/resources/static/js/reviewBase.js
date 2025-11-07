// Toast notification
function showToast(type, message) {
    const toastContainer = document.getElementById("toast-container");
    const toast = document.createElement("div");
    toast.className = `alert ${type === 'success' ? 'alert-success' : 'alert-error'} shadow-lg transition-all duration-300`;
    toast.innerHTML = `
        <div class="flex items-center gap-2">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                ${type === 'success'
            ? '<path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"/>'
            : '<path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"/>'
        }
            </svg>
            <span>${message}</span>
        </div>
    `;
    toastContainer.appendChild(toast);
    setTimeout(() => {
        toast.classList.add("opacity-0", "translate-x-5");
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// Character counter for review textarea
const textReview = document.getElementById("textReview");
const charCount = document.getElementById("charCount");

if (textReview && charCount) {
    textReview.addEventListener("input", () => {
        const length = textReview.value.length;
        charCount.textContent = `${length}/500`;

        if (length > 450) {
            charCount.classList.add("text-error");
        } else {
            charCount.classList.remove("text-error");
        }
    });
}

// Star rating interactive feedback
const ratingInputs = document.querySelectorAll('input[name="rating"]');
ratingInputs.forEach((input, index) => {
    input.addEventListener('change', () => {
        // Highlight all stars up to selected
        const labels = document.querySelectorAll('label[for^="star"]');
        labels.forEach((label, i) => {
            if (i <= index) {
                label.classList.add('text-amber-400');
                label.classList.remove('text-neutral/20');
            } else {
                label.classList.remove('text-amber-400');
                label.classList.add('text-neutral/20');
            }
        });
    });
});

// Review form submission
const reviewForm = document.getElementById("reviewForm");
if (reviewForm) {
    reviewForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const formData = new FormData(reviewForm);
        const submitBtn = reviewForm.querySelector('button[type="submit"]');
        const originalBtnText = submitBtn.innerHTML;

        // Disable button and show loading
        submitBtn.disabled = true;
        submitBtn.innerHTML = `
            <span class="loading loading-spinner loading-sm"></span>
            Mengirim...
        `;

        try {
            const response = await fetch(reviewForm.action, {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                showToast('success', '✅ Review berhasil dikirim!');
                reviewForm.reset();

                // Reset star rating visuals
                document.querySelectorAll('label[for^="star"]').forEach(label => {
                    label.classList.remove('text-amber-400');
                    label.classList.add('text-neutral/20');
                });

                // Reset character counter
                if (charCount) charCount.textContent = '0/500';

                // Reload page after 1.5 seconds
                setTimeout(() => {
                    window.location.reload();
                }, 1500);
            } else {
                const errorText = await response.text();
                showToast('error', '❌ ' + (errorText || 'Gagal mengirim review'));
            }
        } catch (error) {
            console.error('Error submitting review:', error);
            showToast('error', '⚠️ Terjadi kesalahan saat mengirim review');
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalBtnText;
        }
    });
}

// Delete review functionality
let currentButton = null;

async function deleteReview(button) {
    const reviewId = button.getAttribute("data-id");
    if (!reviewId) return;

    currentButton = button;
    const modal = document.getElementById("confirmDeleteModal");
    modal.showModal();
}

document.getElementById("confirmDeleteBtn")?.addEventListener("click", async () => {
    if (!currentButton) return;

    const reviewId = currentButton.getAttribute("data-id");
    const modal = document.getElementById("confirmDeleteModal");
    const confirmBtn = document.getElementById("confirmDeleteBtn");
    const originalBtnText = confirmBtn.innerHTML;

    // Show loading state
    confirmBtn.disabled = true;
    confirmBtn.innerHTML = `
        <span class="loading loading-spinner loading-sm"></span>
        Menghapus...
    `;

    try {
        const res = await fetch(`/admin/reviews/delete/${reviewId}`, {
            method: "DELETE"
        });

        if (res.ok) {
            const reviewCard = currentButton.closest(".card");

            // Animate removal
            reviewCard.style.transition = "all 0.3s ease-out";
            reviewCard.style.opacity = "0";
            reviewCard.style.transform = "translateX(20px)";

            setTimeout(() => {
                reviewCard.remove();
                window.location.reload();
            }, 300);

            showToast("success", "✅ Review berhasil dihapus");
        } else {
            showToast("error", "❌ Gagal menghapus review");
        }
    } catch (err) {
        console.error("Error deleting review:", err);
        showToast("error", "⚠️ Terjadi kesalahan saat menghapus review");
    } finally {
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = originalBtnText;
        modal.close();
        currentButton = null;
    }
});

document.getElementById("cancelDeleteBtn")?.addEventListener("click", () => {
    const modal = document.getElementById("confirmDeleteModal");
    modal.close();
    currentButton = null;
});

// Sort reviews functionality
const sortSelect = document.getElementById("sortReviews");
if (sortSelect) {
    sortSelect.addEventListener("change", (e) => {
        const sortType = e.target.value;
        const reviewsList = document.getElementById("reviewsList");
        const reviews = Array.from(reviewsList.children);

        reviews.sort((a, b) => {
            if (sortType === 'newest') {
                const dateA = new Date(a.querySelector('.text-xs.sm\\:text-sm span')?.textContent || 0);
                const dateB = new Date(b.querySelector('.text-xs.sm\\:text-sm span')?.textContent || 0);
                return dateB - dateA;
            } else if (sortType === 'highest') {
                const ratingA = parseFloat(a.querySelector('.font-bold.text-neutral.ml-1')?.textContent || 0);
                const ratingB = parseFloat(b.querySelector('.font-bold.text-neutral.ml-1')?.textContent || 0);
                return ratingB - ratingA;
            } else if (sortType === 'lowest') {
                const ratingA = parseFloat(a.querySelector('.font-bold.text-neutral.ml-1')?.textContent || 0);
                const ratingB = parseFloat(b.querySelector('.font-bold.text-neutral.ml-1')?.textContent || 0);
                return ratingA - ratingB;
            }
            return 0;
        });

        // Clear and re-append sorted reviews
        reviewsList.innerHTML = '';
        reviews.forEach(review => reviewsList.appendChild(review));

        // Animate entrance
        reviews.forEach((review, index) => {
            review.style.opacity = '0';
            review.style.transform = 'translateY(10px)';
            setTimeout(() => {
                review.style.transition = 'all 0.3s ease-out';
                review.style.opacity = '1';
                review.style.transform = 'translateY(0)';
            }, index * 50);
        });
    });
}
