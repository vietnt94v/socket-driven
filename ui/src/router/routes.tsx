import { lazy } from 'react';
import { Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { RootRedirect } from './RootRedirect';

const Login = lazy(() => import('../pages/login/Login'));
const Home = lazy(() => import('../pages/home/Home'));
const Chat = lazy(() => import('../pages/chat/Chat'));

export const AppRoutes = () => (
  <Routes>
    <Route path="/" element={<RootRedirect />} />
    <Route path="/login" element={<Login />} />
    <Route
      path="/home"
      element={
        <ProtectedRoute>
          <Home />
        </ProtectedRoute>
      }
    />
    <Route
      path="/chat"
      element={
        <ProtectedRoute>
          <Chat />
        </ProtectedRoute>
      }
    />
  </Routes>
);
