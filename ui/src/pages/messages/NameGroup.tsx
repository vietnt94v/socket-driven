import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { createConversation } from '../../apis/conversations';

type LocationState = { memberIds?: string[] };

const NameGroup = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state as LocationState | undefined;
  const memberIds = state?.memberIds ?? [];
  const [name, setName] = useState('');
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (memberIds.length < 2) {
    return (
      <div className="mx-auto max-w-md px-4 py-8">
        <p className="mb-4 text-sm text-neutral-600">
          Thiếu danh sách thành viên. Quay lại để chọn ít nhất hai người.
        </p>
        <Link to="/messages/group" className="text-violet-700">
          ← Chọn lại
        </Link>
      </div>
    );
  }

  const submit = async () => {
    const n = name.trim();
    if (!n) {
      setError('Nhập tên nhóm.');
      return;
    }
    setPending(true);
    setError(null);
    try {
      const conv = await createConversation({
        type: 'GROUP',
        name: n,
        memberIds,
      });
      navigate(`/chat/${conv.id}`, { replace: true });
    } catch {
      setError('Không tạo được nhóm. Thử lại.');
    } finally {
      setPending(false);
    }
  };

  return (
    <div className="mx-auto flex min-h-screen max-w-md flex-col bg-neutral-50 px-4 pb-10 pt-8">
      <h1 className="mb-6 text-center text-lg font-semibold text-neutral-900">
        Đặt tên nhóm
      </h1>
      <input
        className="mb-4 rounded-xl border border-neutral-200 bg-white px-4 py-3 text-neutral-900 outline-none focus:border-violet-500 focus:ring-1 focus:ring-violet-200"
        placeholder="Tên nhóm"
        value={name}
        onChange={(e) => setName(e.target.value)}
        autoFocus
      />
      {error && <p className="mb-3 text-sm text-red-600">{error}</p>}
      <button
        type="button"
        disabled={pending}
        onClick={() => void submit()}
        className="rounded-full bg-violet-600 py-3 font-semibold text-white disabled:opacity-50"
      >
        {pending ? 'Đang tạo…' : 'Tạo nhóm'}
      </button>
      <Link
        to="/messages/group"
        className="mt-4 block text-center text-sm text-violet-700"
      >
        ← Quay lại
      </Link>
    </div>
  );
};

export default NameGroup;
