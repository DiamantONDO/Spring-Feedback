<script setup>
import { ref, onMounted, computed } from "vue";

const API = "http://localhost:8081/api/feedback";

const feedbacks = ref([]);
const average = ref(0);
const loading = ref(false);
const submitting = ref(false);
const error = ref("");
const success = ref("");

const form = ref({
  name: "",
  message: "",
  rating: 0,
});

const hovered = ref(0);

const fetchFeedbacks = async () => {
  loading.value = true;
  try {
    const res = await fetch(API);
    feedbacks.value = await res.json();
    const avgRes = await fetch(`${API}/average`);
    const avgData = await avgRes.json();
    average.value = avgData.average;
  } catch (e) {
    error.value = "Failed to load feedbacks.";
  } finally {
    loading.value = false;
  }
};

const submitFeedback = async () => {
  error.value = "";
  success.value = "";

  if (!form.value.name || !form.value.message || !form.value.rating) {
    error.value = "Please fill in all fields and select a rating.";
    return;
  }

  submitting.value = true;
  try {
    const res = await fetch(API, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(form.value),
    });

    if (res.status === 201) {
      success.value = "Feedback submitted successfully!";
      form.value = { name: "", message: "", rating: 0 };
      hovered.value = 0;
      await fetchFeedbacks();
    } else {
      const data = await res.json();
      error.value = Object.values(data).join(", ");
    }
  } catch (e) {
    error.value = "Failed to submit feedback.";
  } finally {
    submitting.value = false;
  }
};

const deleteFeedback = async (id) => {
  if (!confirm("Delete this feedback?")) return;
  try {
    const res = await fetch(`${API}/${id}`, { method: "DELETE" });
    if (res.ok) await fetchFeedbacks();
  } catch (e) {
    error.value = "Failed to delete feedback.";
  }
};

const ratingLabel = computed(() => {
  const labels = { 1: "Poor", 2: "Fair", 3: "Good", 4: "Very Good", 5: "Excellent" };
  return labels[form.value.rating] || "";
});

const starColor = (index) => {
  const active = hovered.value || form.value.rating;
  return index <= active ? "#f59e0b" : "#d1d5db";
};

const feedbackStarColor = (index, rating) => {
  return index <= rating ? "#f59e0b" : "#e5e7eb";
};

onMounted(fetchFeedbacks);
</script>

<template>
  <div class="app">

    <header class="header">
      <div class="header-inner">
        <div class="logo">
          <span class="logo-icon"></span>
          <span class="logo-text">AnthonyFeedback</span>
        </div>
        <div class="average-pill" v-if="feedbacks.length > 0">
          <span>⭐ Average Rating:</span>
          <strong>{{ average }} / 5</strong>
          <span class="count">({{ feedbacks.length }} reviews)</span>
        </div>
      </div>
    </header>

    <main class="main">

      <div class="form-card">
        <h2 class="form-title">Share Your Feedback</h2>
        <p class="form-subtitle">We'd love to hear what you think</p>

        <div class="alert error" v-if="error">{{ error }}</div>
        <div class="alert success" v-if="success">{{ success }}</div>

        <div class="form-group">
          <label>Your Name</label>
          <input
            v-model="form.name"
            type="text"
            placeholder="e.g. Anthony"
            class="input"
          />
        </div>

        <div class="form-group">
          <label>Your Message</label>
          <textarea
            v-model="form.message"
            placeholder="Tell us about your experience..."
            class="textarea"
            rows="4"
          ></textarea>
        </div>

        <div class="form-group">
          <label>Rating <span class="rating-label">{{ ratingLabel }}</span></label>
          <div class="stars">
            <span
              v-for="i in 5"
              :key="i"
              class="star"
              :style="{ color: starColor(i) }"
              @mouseover="hovered = i"
              @mouseleave="hovered = 0"
              @click="form.rating = i"
            >★</span>
          </div>
        </div>

        <button class="submit-btn" @click="submitFeedback" :disabled="submitting">
          {{ submitting ? "Submitting..." : "Submit Feedback" }}
        </button>
      </div>

      <div class="list-section">
        <h2 class="list-title">
          All Feedback
          <span class="badge">{{ feedbacks.length }}</span>
        </h2>

        <div v-if="loading" class="loading">Loading feedbacks...</div>

        <div v-else-if="feedbacks.length === 0" class="empty">
          No feedback yet. Be the first to share!
        </div>

        <div v-else class="feedback-grid">
          <div
            class="feedback-card"
            v-for="fb in feedbacks"
            :key="fb.id"
          >
            <div class="feedback-header">
              <div class="avatar">{{ fb.name.charAt(0).toUpperCase() }}</div>
              <div>
                <p class="fb-name">{{ fb.name }}</p>
                <div class="fb-stars">
                  <span
                    v-for="i in 5"
                    :key="i"
                    :style="{ color: feedbackStarColor(i, fb.rating) }"
                  >★</span>
                </div>
              </div>
              <button class="delete-btn" @click="deleteFeedback(fb.id)">✕</button>
            </div>
            <p class="fb-message">{{ fb.message }}</p>
            <div class="fb-rating-badge" :class="`rating-${fb.rating}`">
              {{ ["", "Poor", "Fair", "Good", "Very Good", "Excellent"][fb.rating] }}
            </div>
          </div>
        </div>
      </div>

    </main>
  </div>
</template>

<style>
* { box-sizing: border-box; margin: 0; padding: 0; }

body {
  font-family: 'Segoe UI', Arial, sans-serif;
  background: #f0f4ff;
  min-height: 100vh;
}

.app { min-height: 100vh; }

.header {
  background: linear-gradient(135deg, #1e3a8a, #3b82f6);
  padding: 16px 40px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.15);
}
.header-inner {
  max-width: 1100px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo { display: flex; align-items: center; gap: 10px; }
.logo-icon { font-size: 28px; }
.logo-text { font-size: 22px; font-weight: 700; color: white; }
.average-pill {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255,255,255,0.15);
  color: white;
  padding: 8px 18px;
  border-radius: 20px;
  font-size: 14px;
}
.average-pill strong { font-size: 16px; color: #fde68a; }
.count { opacity: 0.7; font-size: 12px; }

.main {
  max-width: 1100px;
  margin: 40px auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 30px;
  align-items: start;
}

.form-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  position: sticky;
  top: 20px;
}
.form-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e3a8a;
  margin-bottom: 4px;
}
.form-subtitle {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 24px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 18px;
}
.form-group label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 8px;
}
.rating-label {
  font-weight: 400;
  color: #f59e0b;
  font-size: 12px;
}
.input, .textarea {
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  padding: 11px 14px;
  font-size: 14px;
  transition: border 0.2s;
  outline: none;
  font-family: inherit;
}
.input:focus, .textarea:focus { border-color: #3b82f6; }
.textarea { resize: vertical; }

.stars { display: flex; gap: 6px; }
.star {
  font-size: 32px;
  cursor: pointer;
  transition: transform 0.1s;
}
.star:hover { transform: scale(1.2); }

.submit-btn {
  width: 100%;
  background: linear-gradient(135deg, #1e3a8a, #3b82f6);
  color: white;
  border: none;
  padding: 14px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
  margin-top: 6px;
}
.submit-btn:hover { opacity: 0.9; }
.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.alert {
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 13px;
  margin-bottom: 16px;
}
.error { background: #fee2e2; color: #b91c1c; }
.success { background: #dcfce7; color: #15803d; }

.list-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e3a8a;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.badge {
  background: #1e3a8a;
  color: white;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 13px;
}
.loading, .empty {
  text-align: center;
  color: #9ca3af;
  padding: 40px;
  font-size: 15px;
}

.feedback-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.feedback-card {
  background: white;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.06);
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
}
.feedback-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.1);
}
.feedback-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1e3a8a, #3b82f6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
}
.fb-name { font-weight: 600; font-size: 14px; color: #111827; }
.fb-stars { font-size: 16px; }
.delete-btn {
  margin-left: auto;
  background: #fee2e2;
  color: #dc2626;
  border: none;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.delete-btn:hover { background: #dc2626; color: white; }
.fb-message {
  font-size: 13px;
  color: #4b5563;
  line-height: 1.6;
  margin-bottom: 12px;
}
.fb-rating-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}
.rating-1 { background: #fee2e2; color: #dc2626; }
.rating-2 { background: #ffedd5; color: #f97316; }
.rating-3 { background: #fef9c3; color: #ca8a04; }
.rating-4 { background: #dbeafe; color: #1d4ed8; }
.rating-5 { background: #dcfce7; color: #16a34a; }

@media (max-width: 768px) {
  .main { grid-template-columns: 1fr; }
  .feedback-grid { grid-template-columns: 1fr; }
  .average-pill { display: none; }
}
</style>