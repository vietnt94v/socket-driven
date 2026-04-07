const PALETTE = [
  'bg-emerald-200 text-emerald-900',
  'bg-violet-200 text-violet-900',
  'bg-amber-200 text-amber-900',
  'bg-sky-200 text-sky-900',
  'bg-rose-200 text-rose-900',
];

function paletteIndex(seed: string): number {
  let h = 0;
  for (let i = 0; i < seed.length; i += 1) {
    h = (h * 31 + seed.charCodeAt(i)) >>> 0;
  }
  return h % PALETTE.length;
}

export type AvatarProps = {
  image?: string | null;
  lastName?: string;
  firstName?: string;
  displayName?: string;
  username?: string;
  className?: string;
  size?: 'sm' | 'md' | 'lg';
};

export function Avatar({
  image,
  lastName,
  firstName,
  displayName,
  username,
  className = '',
  size = 'md',
}: AvatarProps) {
  const lastFromDisplay =
    displayName?.trim().split(/\s+/).filter(Boolean).pop() ?? '';
  const raw =
    lastName?.trim() ||
    firstName?.trim() ||
    lastFromDisplay ||
    displayName?.trim() ||
    username?.trim() ||
    '?';
  const letter = raw.charAt(0).toUpperCase();
  const seed = username ?? displayName ?? raw;
  const palette = PALETTE[paletteIndex(seed)];
  const sizeCls =
    size === 'sm'
      ? 'h-9 w-9 min-h-9 min-w-9 text-xs'
      : size === 'lg'
        ? 'h-14 w-14 min-h-14 min-w-14 text-lg'
        : 'h-11 w-11 min-h-11 min-w-11 text-sm';

  if (image) {
    return (
      <img
        src={image}
        alt=""
        className={`${sizeCls} shrink-0 rounded-full object-cover ${className}`}
      />
    );
  }

  return (
    <div
      className={`flex shrink-0 items-center justify-center rounded-full font-semibold ${palette} ${sizeCls} ${className}`}
      aria-hidden
    >
      {letter}
    </div>
  );
}
