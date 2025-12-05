import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { UsersComponent } from './users.component';

describe('UsersComponent', () => {
  let component: UsersComponent;
  let fixture: ComponentFixture<UsersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UsersComponent ],
      imports: [ FormsModule ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have initial users list', () => {
    expect(component.users.length).toBeGreaterThan(0);
  });

  it('should open create modal', () => {
    component.handleCreate();
    expect(component.showModal).toBeTruthy();
    expect(component.modalType).toBe('create');
  });

  it('should open update modal with user data', () => {
    const testUser = component.users[0];
    component.handleUpdate(testUser);
    expect(component.showModal).toBeTruthy();
    expect(component.modalType).toBe('update');
    expect(component.selectedUser).toEqual(testUser);
  });

  it('should open view modal', () => {
    const testUser = component.users[0];
    component.handleView(testUser);
    expect(component.showModal).toBeTruthy();
    expect(component.modalType).toBe('view');
    expect(component.selectedUser).toEqual(testUser);
  });

  it('should delete user', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    const initialLength = component.users.length;
    const userIdToDelete = component.users[0].id;
    
    component.handleDelete(userIdToDelete);
    
    expect(component.users.length).toBe(initialLength - 1);
    expect(component.users.find(u => u.id === userIdToDelete)).toBeUndefined();
  });

  it('should not delete user if not confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(false);
    const initialLength = component.users.length;
    
    component.handleDelete(component.users[0].id);
    
    expect(component.users.length).toBe(initialLength);
  });

  it('should create new user', () => {
    component.modalType = 'create';
    component.formData = {
      name: 'Test User',
      email: 'test@example.com',
      role: 'User',
      status: 'Active'
    };
    const initialLength = component.users.length;
    
    component.handleSubmit();
    
    expect(component.users.length).toBe(initialLength + 1);
    expect(component.showModal).toBeFalsy();
  });

  it('should update existing user', () => {
    const userToUpdate = component.users[0];
    component.modalType = 'update';
    component.selectedUser = userToUpdate;
    component.formData = {
      name: 'Updated Name',
      email: userToUpdate.email,
      role: userToUpdate.role,
      status: userToUpdate.status
    };
    
    component.handleSubmit();
    
    const updatedUser = component.users.find(u => u.id === userToUpdate.id);
    expect(updatedUser?.name).toBe('Updated Name');
    expect(component.showModal).toBeFalsy();
  });

  it('should not submit if required fields are empty', () => {
    spyOn(window, 'alert');
    component.modalType = 'create';
    component.formData = { name: '', email: '' };
    
    component.handleSubmit();
    
    expect(window.alert).toHaveBeenCalledWith('Vui lòng điền đầy đủ thông tin!');
    expect(component.showModal).toBeTruthy();
  });

  it('should close modal', () => {
    component.showModal = true;
    component.selectedUser = component.users[0];
    
    component.closeModal();
    
    expect(component.showModal).toBeFalsy();
    expect(component.selectedUser).toBeNull();
  });

  it('should return correct badge class for Admin role', () => {
    const badgeClass = component.getRoleBadgeClass('Admin');
    expect(badgeClass).toBe('bg-purple-100 text-purple-700');
  });

  it('should return correct badge class for Editor role', () => {
    const badgeClass = component.getRoleBadgeClass('Editor');
    expect(badgeClass).toBe('bg-blue-100 text-blue-700');
  });

  it('should return correct badge class for User role', () => {
    const badgeClass = component.getRoleBadgeClass('User');
    expect(badgeClass).toBe('bg-gray-100 text-gray-700');
  });

  it('should return correct badge class for Active status', () => {
    const badgeClass = component.getStatusBadgeClass('Active');
    expect(badgeClass).toBe('bg-green-100 text-green-700');
  });

  it('should return correct badge class for Inactive status', () => {
    const badgeClass = component.getStatusBadgeClass('Inactive');
    expect(badgeClass).toBe('bg-red-100 text-red-700');
  });
});