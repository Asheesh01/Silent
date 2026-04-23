import { useState, useEffect } from 'react'
import Navbar from './components/NavBar1/NavBar1'
import Landing from './pages/Landingpage/Landingpage'
import Footer from './components/Footer/footer'
import { Navigate, Route, Router, Routes, useNavigate } from 'react-router-dom'
import Signup from './pages/Sign up/signup'
import Login from './pages/Login/Login'
import Navbar2 from './components/Navbar2/Navbar2'
import Feeds from './pages/Feed/Feeds'
import MyNetwork from './pages/MyNetwork/MyNetwork'
import Resume from './pages/Resume/Resume'
import Message from './pages/Meassage/Message'
import Profile from './pages/Profile/Profile'
import Allactivities from './pages/Allactivities/Allactivites'
import SingleActivity from './pages/Singleactivity/SingleActivity'
import Notification from './pages/Notification/Notification'
function App() {
  const [isLogin, setIsLogin] = useState(localStorage.getItem("isLogin"));
  const changeLoginValue = (val) => {
    setIsLogin(val)
  }
  return (
    <div>
      {isLogin ? <Navbar2 /> : <Navbar />}
      <Routes>
        <Route path='/' element={isLogin ? <Navigate to={'/feed'} /> : <Landing changeLoginValue={changeLoginValue} />} />

        <Route path='/signUp' element={isLogin ? <Navigate to={'/feed'} /> : <Signup changeLoginValue={changeLoginValue} />} />

        <Route path='/Login' element={isLogin ? <Navigate to={'/feed'} /> : <Login changeLoginValue={changeLoginValue} />} />

        <Route path='/feed' element={isLogin ? <Feeds /> : <Navigate to={'/Login'} />} />

        <Route path='/mynetwork' element={isLogin ? <MyNetwork /> : <Navigate to={'/Login'} />} />

        <Route path='/resume' element={isLogin ? <Resume /> : <Navigate to={'/Login'} />} />

        <Route path='/message' element={isLogin ? <Message /> : <Navigate to={'/Login'} />} />

        <Route path='/profile/:id' element={isLogin ? <Profile /> : <Navigate to={'/Login'} />} />

        <Route path='/profile/:id/activities' element={isLogin ? <Allactivities /> : <Navigate to={'/Login'} />} />

        <Route path='/profile/:id/activities/:postId' element={isLogin ? <SingleActivity /> : <Navigate to={'/Login'} />} />

        <Route path='/Notifications' element={isLogin ? <Notification /> : <Navigate to={'/Login'} />} />
      </Routes>
      <Footer />
    </div>
  )
}

export default App
