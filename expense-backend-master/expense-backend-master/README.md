# Expense Tracker Frontend

A mobile-responsive web application for tracking personal expenses and income, built with HTML, CSS, JavaScript, and Bootstrap. This frontend integrates with your Spring Boot expense backend API.

## Features

### 🎯 Core Functionality
- **User Authentication**: Register and login with username/password
- **Expense Tracking**: Add expenses with categories and descriptions
- **Income Management**: Track income sources and amounts
- **Balance Calculation**: Real-time balance updates (Income - Expenses)
- **Expense History**: View categorized expense history with expandable cards
- **Progress Tracking**: Visual progress bar for expense usage

### 📱 Mobile-First Design
- Fully responsive design that works on all devices
- Mobile-optimized interface matching your reference image
- Touch-friendly interactions
- Bottom navigation for easy access

### 🎨 UI/UX Features
- Clean, modern interface with Bootstrap 5
- Font Awesome icons for better visual appeal
- Smooth animations and transitions
- Toast notifications for user feedback
- Loading spinners for better UX
- Real-time clock display

## File Structure

```
expense-tracker-frontend/
├── index.html          # Main HTML file
├── styles.css          # Custom CSS styles
├── script.js           # JavaScript functionality
└── README.md           # This file
```

## Setup Instructions

### Prerequisites
1. Your Spring Boot backend should be running on `http://localhost:8084`
2. A modern web browser (Chrome, Firefox, Safari, Edge)
3. No additional dependencies required (uses CDN for Bootstrap and Font Awesome)

### Running the Application

1. **Start your backend server**:
   ```bash
   # Navigate to your Spring Boot project directory
   cd expense-backend
   
   # Run the application
   mvn spring-boot:run
   ```

2. **Open the frontend**:
   - Simply open `index.html` in your web browser
   - Or serve it using a local server:
     ```bash
     # Using Python 3
     python -m http.server 8000
     
     # Using Node.js (if you have http-server installed)
     npx http-server
     ```

3. **Access the application**:
   - If using a local server: `http://localhost:8000`
   - If opening directly: `file:///path/to/your/index.html`

## API Integration

The frontend integrates with all your backend APIs:

### Authentication
- `POST /user/signUp` - User registration
- `POST /user/login` - User login

### Expenses
- `POST /api/expense/{username}` - Add new expense
- `GET /api/history/{userId}` - Get expense history
- `GET /api/balance/{userId}` - Get current balance

### Income
- `POST /income/{username}` - Add new income
- `GET /income/{username}` - Get income history

### Categories
- `GET /api/categories?username={username}` - Get user categories

## Usage Guide

### 1. Registration & Login
- **Register**: Fill in your name, username, password, and initial balance
- **Login**: Use your username and password to access the dashboard

### 2. Adding Expenses
1. Enter the expense amount (default: ₹100)
2. Select a category from the dropdown
3. Add an optional description
4. Click "Submit" to save the expense

### 3. Adding Income
1. Switch to the "Income" tab using bottom navigation
2. Enter the income amount
3. Select an income category
4. Add an optional description
5. Click "Add Income" to save

### 4. Viewing History
- Expense history is automatically grouped by category
- Click on category cards to expand/collapse details
- Each expense shows description, date, and amount

### 5. Navigation
- Use the bottom navigation to switch between Expense and Income tabs
- The hamburger menu button is available for future features

## Customization

### Changing API Base URL
Edit the `API_BASE_URL` constant in `script.js`:
```javascript
const API_BASE_URL = 'http://localhost:8084/loop-service';
```

### Styling Modifications
- Colors and themes can be modified in `styles.css`
- Bootstrap classes can be customized or extended
- Font Awesome icons can be changed in the HTML

### Adding New Features
- The modular JavaScript structure makes it easy to add new functionality
- API calls are centralized in the `apiCall()` function
- Event listeners are set up in `setupEventListeners()`

## Browser Compatibility

- ✅ Chrome 60+
- ✅ Firefox 55+
- ✅ Safari 12+
- ✅ Edge 79+
- ✅ Mobile browsers (iOS Safari, Chrome Mobile)

## Troubleshooting

### Common Issues

1. **CORS Errors**:
   - Ensure your backend has CORS configured
   - Check that the API base URL is correct

2. **API Connection Issues**:
   - Verify your backend is running on port 8084
   - Check browser console for error messages

3. **Styling Issues**:
   - Ensure all CSS and JavaScript files are in the same directory
   - Check that CDN links are accessible

4. **Mobile Responsiveness**:
   - Test on different screen sizes
   - Use browser developer tools to simulate mobile devices

### Debug Mode
The application shows "DEBUG" in the header when running, which helps identify development builds.

## Future Enhancements

Potential features you could add:
- Budget setting and tracking
- Expense analytics and charts
- Export functionality (PDF/Excel)
- Multiple currency support
- Receipt image upload
- Recurring expense setup
- Expense sharing between users

## Support

If you encounter any issues:
1. Check the browser console for JavaScript errors
2. Verify your backend API is running and accessible
3. Ensure all files are in the correct directory structure
4. Test with a different browser to rule out browser-specific issues

---

**Note**: This frontend is designed to work specifically with your Spring Boot expense backend. Make sure your backend is running and accessible before testing the frontend functionality.
