# API Integration - Vehicle Images

## Tóm tắt thay đổi

### 1. Thêm Glide Library (app/build.gradle.kts)
- Thêm Glide để load ảnh từ URL API
- Glide version 4.16.0

### 2. Cập nhật VehicleAdapter
**File**: `app/src/main/java/com/example/prm_assignment/ui/adapters/VehicleAdapter.java`

#### Thay đổi trong Vehicle class:
- Thêm field `imageUrl` để lưu URL ảnh từ API
- Tạo 2 constructors:
  - Constructor với `int imageResId` (cho ảnh local/resource)
  - Constructor với `String imageUrl` (cho ảnh từ API)

#### Thay đổi trong onBindViewHolder:
- Sử dụng Glide để load ảnh từ URL
- Logic load ảnh:
  1. Nếu có `imageUrl` → load từ URL bằng Glide
  2. Nếu có `imageResId` → load từ resource
  3. Nếu không có gì → hiển thị placeholder
- Thêm placeholder và error handling

### 3. Cập nhật VehiclesFragment
**File**: `app/src/main/java/com/example/prm_assignment/ui/fragments/VehiclesFragment.java`

#### Thay đổi trong convertToAdapterVehicles:
- Lấy `imageUrl` từ `model.getImage()`
- Sử dụng constructor mới của Vehicle với `imageUrl` thay vì `imageResId`

### 4. Tạo Placeholder Image
**File**: `app/src/main/res/drawable/ic_car_placeholder.xml`
- Tạo vector drawable cho placeholder khi:
  - Ảnh đang load
  - Ảnh load lỗi
  - Không có ảnh từ API

## Cách hoạt động

1. API trả về dữ liệu vehicle với field `image` chứa URL
2. VehiclesFragment nhận data và convert sang Vehicle adapter model
3. Vehicle adapter model lưu `imageUrl` từ API
4. VehicleAdapter sử dụng Glide để:
   - Load ảnh từ URL
   - Hiển thị placeholder trong khi đang load
   - Hiển thị placeholder nếu load lỗi
   - Cache ảnh để tăng performance

## Lợi ích của Glide

- **Tự động cache**: Ảnh được cache tự động để tăng tốc
- **Memory management**: Quản lý memory hiệu quả, tránh memory leak
- **Placeholder support**: Hiển thị ảnh placeholder trong khi load
- **Error handling**: Xử lý lỗi khi load ảnh thất bại
- **Image transformations**: Hỗ trợ centerCrop, fitCenter, v.v.

## Test

Để test xem ảnh có load không:
1. Login vào app
2. Vào màn hình Vehicles
3. API sẽ được gọi và load danh sách xe
4. Ảnh của mỗi xe sẽ được load từ URL trong field `image`
5. Nếu URL hợp lệ → hiển thị ảnh xe
6. Nếu URL null/empty hoặc lỗi → hiển thị placeholder

## Lưu ý

- Đảm bảo app có quyền INTERNET trong AndroidManifest.xml
- URL ảnh từ API phải accessible (không bị CORS hoặc authentication block)
- Glide tự động resize ảnh để fit vào ImageView

