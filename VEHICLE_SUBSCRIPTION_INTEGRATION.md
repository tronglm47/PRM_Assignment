# Tích hợp API Vehicle Subscriptions vào Home Screen

## Tóm tắt
Đã tích hợp thành công API `/vehicle-subscriptions/customer/{customerId}` vào màn hình Home để hiển thị danh sách các gói dịch vụ đã mua cho từng xe của khách hàng.

## Các file đã tạo mới

### 1. Model Classes
- **VehicleSubscriptionResponse.java**: Model chứa thông tin subscription của xe
  - `VehicleSubscription`: Thông tin gói đăng ký
  - `VehicleInfo`: Thông tin xe (tên, model, VIN, imageUrl)
  - `PackageInfo`: Thông tin gói dịch vụ (tên, mô tả, giá, thời gian, km)

- **CustomerResponse.java**: Model để lấy customerId từ userId
  - Endpoint: `/customers/user/{userId}`
  - Trả về `customerId` trong field `data._id`

### 2. API Interfaces
- **VehicleSubscriptionApi.java**: Interface cho API vehicle subscriptions
  ```java
  @GET("vehicle-subscriptions/customer/{customerId}")
  Call<VehicleSubscriptionResponse> getCustomerSubscriptions(
      @Path("customerId") String customerId,
      @Header("Authorization") String authHeader
  );
  ```

- **CustomerApi.java**: Interface cho API customer
  ```java
  @GET("customers/user/{userId}")
  Call<CustomerResponse> getCustomerByUserId(
      @Path("userId") String userId,
      @Header("Authorization") String authHeader
  );
  ```

### 3. Retrofit Clients
- **VehicleSubscriptionRetrofitClient.java**: Singleton client cho vehicle subscription API
- **CustomerRetrofitClient.java**: Singleton client cho customer API

### 4. Layout Files
- **item_vehicle_subscription.xml**: Layout cho mỗi card hiển thị subscription
  - Ảnh xe (80x80dp)
  - Tên xe và model
  - Thông tin gói dịch vụ
  - Trạng thái còn bao nhiêu ngày
  - Khoảng cách km
  - Progress bar hiển thị % thời gian còn lại
  - Ngày bắt đầu - kết thúc

- **rounded_background.xml**: Drawable cho background bo tròn

## Các file đã chỉnh sửa

### 1. TokenHelper.kt
Thêm các interface và method mới:
- `CustomerIdCallback`: Interface cho callback trả về customerId
- `getCustomerIdFromProfile()`: Method lấy customerId theo flow:
  1. Gọi `/auth/profile` để lấy userId
  2. Gọi `/customers/user/{userId}` để lấy customerId từ userId
  3. Trả về customerId qua callback

### 2. HomeFragment.java
Thêm các chức năng:
- `llVehicleSubscriptionsContainer`: Container để chứa các card subscription
- `vehicleSubscriptionsLoaded`: Flag theo dõi loading state
- `loadVehicleSubscriptions()`: Method load danh sách subscriptions
- `fetchVehicleSubscriptions()`: Method gọi API lấy subscriptions
- `addVehicleSubscriptionCard()`: Method thêm card subscription vào UI
  - Hiển thị ảnh xe (sử dụng Glide)
  - Tính số ngày còn lại
  - Hiển thị progress bar với màu sắc theo mức độ khẩn cấp:
    - Xanh lá: > 15 ngày
    - Cam: 8-15 ngày
    - Đỏ: ≤ 7 ngày
  - Format ngày tháng sang dd/MM/yyyy

### 3. fragment_home.xml
- Thay thế các card xe hardcoded bằng container động `llVehicleSubscriptionsContainer`
- Cards sẽ được tạo từ API data

## Flow hoạt động

1. **User mở Home screen**
2. **Load profile data**: Hiển thị tên và địa chỉ khách hàng
3. **Load packages**: Hiển thị các gói dịch vụ có sẵn
4. **Load vehicle subscriptions**:
   - Gọi `getCustomerIdFromProfile()` để lấy customerId
   - Gọi API `/vehicle-subscriptions/customer/{customerId}` với Bearer token
   - Parse response thành list VehicleSubscription
   - Với mỗi subscription, tạo một card và add vào container
   - Load ảnh xe bằng Glide (nếu có imageUrl)
   - Tính toán và hiển thị số ngày còn lại
   - Set màu progress bar theo mức độ khẩn cấp

## Tính năng chính

### Hiển thị thông tin xe
- Ảnh xe (load từ imageUrl hoặc dùng icon mặc định)
- Tên xe
- Model xe
- Tên gói dịch vụ đã đăng ký

### Hiển thị trạng thái subscription
- Số ngày còn lại (tính từ endDate - ngày hiện tại)
- Khoảng cách km (từ package.km_interval)
- Progress bar thể hiện % thời gian còn lại
- Màu sắc thay đổi theo độ khẩn cấp
- Ngày bắt đầu và kết thúc (format: dd/MM/yyyy)

### Loading state management
- Hiển thị loading overlay khi đang tải dữ liệu
- Chỉ ẩn loading khi cả 3 nguồn dữ liệu đã load xong:
  1. Profile
  2. Packages
  3. Vehicle subscriptions

## Thư viện sử dụng
- **Retrofit 2**: Gọi REST API
- **Gson**: Parse JSON
- **Glide**: Load và cache ảnh từ URL
- **Material Components**: UI components

## Lưu ý quan trọng

### API Flow
```
1. auth/profile → userId
2. customers/user/{userId} → customerId  
3. vehicle-subscriptions/customer/{customerId} → subscription list
```

### Xử lý ảnh
- Nếu có `imageUrl`: Load bằng Glide, loại bỏ tint
- Nếu không có: Dùng icon mặc định với màu xanh lá

### Date parsing
- Input format: `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'` (UTC)
- Output format: `dd/MM/yyyy`
- Timezone: UTC

## Testing
Build project bằng lệnh:
```bash
cd D:\Semester_8\PRM_Assignment
.\gradlew assembleDebug
```

Sau khi build thành công, test trên emulator/device với:
1. Login với tài khoản có vehicle subscriptions
2. Kiểm tra Home screen hiển thị danh sách xe
3. Verify thông tin hiển thị đúng (tên xe, ngày, progress bar)
4. Kiểm tra ảnh xe load đúng (nếu API trả về imageUrl)

