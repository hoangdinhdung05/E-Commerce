import { Component } from '@angular/core';

@Component({
  selector: 'app-not-found',
  template: `
    <div class="not-found-container">
      <div class="not-found-content">
        <div class="error-code">404</div>
        <div class="error-icon">
          <i class="bi bi-exclamation-triangle"></i>
        </div>
        <h1 class="error-title">Trang không tìm thấy</h1>
        <p class="error-message">
          Rất tiếc! Trang bạn đang tìm kiếm không tồn tại hoặc đã bị di chuyển.
        </p>
        <div class="action-buttons">
          <a routerLink="/" class="btn btn-primary">
            <i class="bi bi-house-door"></i>
            Về trang chủ
          </a>
          <button class="btn btn-secondary" onclick="history.back()">
            <i class="bi bi-arrow-left"></i>
            Quay lại
          </button>
        </div>
      </div>
      <div class="decoration-circles">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
        <div class="circle circle-3"></div>
      </div>
    </div>
  `,
  styles: [`
    .not-found-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #f5f7fa 0%, #e8f0fe 100%);
      position: relative;
      overflow: hidden;
      padding: 20px;
    }

    .not-found-content {
      text-align: center;
      position: relative;
      z-index: 2;
      max-width: 600px;
      animation: fadeInUp 0.6s ease-out;
    }

    .error-code {
      font-size: 140px;
      font-weight: 800;
      background: linear-gradient(135deg, #1890ff, #40a9ff);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      line-height: 1;
      margin-bottom: 20px;
      text-shadow: 0 4px 20px rgba(24, 144, 255, 0.2);
      animation: float 3s ease-in-out infinite;
    }

    .error-icon {
      font-size: 80px;
      color: #1890ff;
      margin-bottom: 20px;
      animation: bounce 2s ease-in-out infinite;
    }

    .error-title {
      font-size: 32px;
      font-weight: 700;
      color: #333;
      margin-bottom: 16px;
      letter-spacing: -0.5px;
    }

    .error-message {
      font-size: 16px;
      color: #666;
      margin-bottom: 40px;
      line-height: 1.6;
      max-width: 500px;
      margin-left: auto;
      margin-right: auto;
    }

    .action-buttons {
      display: flex;
      gap: 16px;
      justify-content: center;
      flex-wrap: wrap;
    }

    .btn {
      padding: 12px 28px;
      border-radius: 10px;
      font-size: 15px;
      font-weight: 600;
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      transition: all 0.3s ease;
      border: none;
      cursor: pointer;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .btn-primary {
      background: linear-gradient(135deg, #1890ff, #40a9ff);
      color: white;
    }

    .btn-primary:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(24, 144, 255, 0.4);
    }

    .btn-secondary {
      background: white;
      color: #1890ff;
      border: 2px solid #1890ff;
    }

    .btn-secondary:hover {
      background: #1890ff;
      color: white;
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(24, 144, 255, 0.3);
    }

    /* Decoration circles */
    .decoration-circles {
      position: absolute;
      width: 100%;
      height: 100%;
      top: 0;
      left: 0;
      z-index: 1;
      pointer-events: none;
    }

    .circle {
      position: absolute;
      border-radius: 50%;
      background: linear-gradient(135deg, rgba(24, 144, 255, 0.1), rgba(64, 169, 255, 0.05));
      animation: float 6s ease-in-out infinite;
    }

    .circle-1 {
      width: 300px;
      height: 300px;
      top: -100px;
      right: -100px;
      animation-delay: 0s;
    }

    .circle-2 {
      width: 200px;
      height: 200px;
      bottom: -50px;
      left: -50px;
      animation-delay: 2s;
    }

    .circle-3 {
      width: 150px;
      height: 150px;
      top: 50%;
      left: 10%;
      animation-delay: 4s;
    }

    /* Animations */
    @keyframes fadeInUp {
      from {
        opacity: 0;
        transform: translateY(30px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    @keyframes float {
      0%, 100% {
        transform: translateY(0) scale(1);
      }
      50% {
        transform: translateY(-20px) scale(1.05);
      }
    }

    @keyframes bounce {
      0%, 100% {
        transform: translateY(0);
      }
      50% {
        transform: translateY(-10px);
      }
    }

    /* Responsive */
    @media (max-width: 768px) {
      .error-code {
        font-size: 100px;
      }

      .error-icon {
        font-size: 60px;
      }

      .error-title {
        font-size: 24px;
      }

      .error-message {
        font-size: 14px;
      }

      .btn {
        padding: 10px 20px;
        font-size: 14px;
      }

      .circle-1, .circle-2, .circle-3 {
        opacity: 0.5;
      }
    }
  `]
})
export class NotFoundComponent {}
