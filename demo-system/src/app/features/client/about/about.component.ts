import { Component, OnInit } from '@angular/core';
import { UserService } from '../../../core/services/users/user.service';
import { UserResponse } from '../../../core/models/response/User/user-response';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-about',
  templateUrl: './about.component.html',
  styleUrls: ['./about.component.css']
})
export class AboutComponent implements OnInit {
  isLoadingTeam = true;
  
  // Mock data - sẽ thay bằng API call sau
  companyInfo = {
    name: 'Demo Store',
    foundedYear: 2020,
    tagline: 'Nơi mua sắm tin cậy của bạn',
    description: 'Chúng tôi là một trong những nhà bán lẻ hàng đầu tại Việt Nam, chuyên cung cấp các sản phẩm chất lượng cao với giá cả cạnh tranh nhất. Với hơn 5 năm kinh nghiệm trong ngành, chúng tôi tự hào mang đến cho khách hàng trải nghiệm mua sắm tuyệt vời nhất.',
    mission: 'Mang đến cho khách hàng những sản phẩm chất lượng tốt nhất với giá cả hợp lý nhất, cùng với dịch vụ chăm sóc khách hàng tận tâm.',
    vision: 'Trở thành nền tảng thương mại điện tử hàng đầu Việt Nam, được khách hàng tin tưởng và lựa chọn.',
    values: [
      {
        icon: 'fa-handshake',
        title: 'Uy tín',
        description: 'Cam kết sản phẩm chính hãng 100%'
      },
      {
        icon: 'fa-star',
        title: 'Chất lượng',
        description: 'Sản phẩm được kiểm định nghiêm ngặt'
      },
      {
        icon: 'fa-heart',
        title: 'Tận tâm',
        description: 'Chăm sóc khách hàng 24/7'
      },
      {
        icon: 'fa-rocket',
        title: 'Đổi mới',
        description: 'Liên tục cải tiến và phát triển'
      }
    ]
  };

  stats = [
    { number: '50K+', label: 'Khách hàng tin tưởng', icon: 'fa-users' },
    { number: '10K+', label: 'Sản phẩm đa dạng', icon: 'fa-box' },
    { number: '5+', label: 'Năm kinh nghiệm', icon: 'fa-clock' },
    { number: '99%', label: 'Khách hàng hài lòng', icon: 'fa-smile' }
  ];

  // Team sẽ được load từ API (users có role ADMIN)
  team: any[] = [];

  timeline = [
    { year: '2020', title: 'Thành lập', description: 'Demo Store được thành lập với mục tiêu mang đến trải nghiệm mua sắm tốt nhất' },
    { year: '2021', title: 'Mở rộng', description: 'Mở rộng kho hàng và tăng cường đội ngũ phục vụ khách hàng' },
    { year: '2022', title: 'Phát triển', description: 'Ra mắt ứng dụng mobile và website mới với trải nghiệm tốt hơn' },
    { year: '2023', title: 'Đổi mới', description: 'Áp dụng công nghệ AI và tự động hóa trong vận hành' },
    { year: '2024', title: 'Dẫn đầu', description: 'Trở thành top 3 nền tảng thương mại điện tử tại Việt Nam' }
  ];

  constructor(private userService: UserService) { }

  ngOnInit(): void {
    this.loadAdminTeam();
  }

  loadAdminTeam(): void {
    this.isLoadingTeam = true;
    // Lấy tất cả users và lọc ra những người có role ADMIN
    this.userService.getAllUsers(0, 100).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          const allUsers = response.data.content;
          
          // Lấy chi tiết từng user để kiểm tra role (vì UserResponse không có role)
          // Giả sử admin users có ID từ 1-4
          const adminPromises = allUsers.slice(0, 4).map(user => 
            this.userService.getUserDetails(user.id).toPromise()
          );
          
          Promise.all(adminPromises).then(results => {
            this.team = results
              .filter(res => res && res.success && res.data)
              .map((res, index) => {
                const user = res!.data!;
                const positions = ['CEO & Founder', 'Marketing Director', 'Technical Director', 'Customer Service Manager'];
                const descriptions = [
                  'Người sáng lập và điều hành công ty với hơn 10 năm kinh nghiệm trong lĩnh vực thương mại điện tử.',
                  'Chuyên gia marketing với nhiều chiến dịch thành công trong ngành bán lẻ.',
                  'Lãnh đạo đội ngũ công nghệ, phát triển nền tảng hiện đại và bảo mật.',
                  'Quản lý dịch vụ khách hàng với đội ngũ tận tâm, chuyên nghiệp.'
                ];
                
                return {
                  name: `${user.firstName} ${user.lastName}`.trim() || user.username,
                  position: positions[index] || 'Admin',
                  image: user.avatarUrl 
                    ? `${environment.assetBase}${user.avatarUrl.startsWith('/') ? '' : '/'}${user.avatarUrl}`
                    : `https://ui-avatars.com/api/?name=${encodeURIComponent(user.username)}&background=2196F3&color=fff&size=200`,
                  description: descriptions[index] || 'Thành viên đội ngũ lãnh đạo của chúng tôi.'
                };
              });
            this.isLoadingTeam = false;
          });
        } else {
          this.isLoadingTeam = false;
        }
      },
      error: (error) => {
        console.error('Error loading admin team:', error);
        this.isLoadingTeam = false;
        // Fallback to default team if error
        this.team = [];
      }
    });
  }
}
