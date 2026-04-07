import { lazy } from 'react';
import { Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { RootRedirect } from './RootRedirect';

const Login = lazy(() => import('../pages/login/Login'));
const Home = lazy(() => import('../pages/home/Home'));
const Chat = lazy(() => import('../pages/chat/Chat'));
const MessagesInbox = lazy(() => import('../pages/messages/MessagesInbox'));
const NewChat = lazy(() => import('../pages/messages/NewChat'));
const CreateGroup = lazy(() => import('../pages/messages/CreateGroup'));
const NameGroup = lazy(() => import('../pages/messages/NameGroup'));

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
      path="/messages"
      element={
        <ProtectedRoute>
          <MessagesInbox />
        </ProtectedRoute>
      }
    />
    <Route
      path="/messages/new"
      element={
        <ProtectedRoute>
          <NewChat />
        </ProtectedRoute>
      }
    />
    <Route
      path="/messages/group"
      element={
        <ProtectedRoute>
          <CreateGroup />
        </ProtectedRoute>
      }
    />
    <Route
      path="/messages/group/name"
      element={
        <ProtectedRoute>
          <NameGroup />
        </ProtectedRoute>
      }
    />
    <Route
      path="/chat/:conversationId"
      element={
        <ProtectedRoute>
          <Chat />
        </ProtectedRoute>
      }
    />
  </Routes>
);
