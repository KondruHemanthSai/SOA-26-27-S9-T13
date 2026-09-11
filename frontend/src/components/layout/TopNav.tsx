interface TopNavProps {
  searchPlaceholder?: string;
  profileImageUrl?: string;
}

export default function TopNav({ searchPlaceholder = 'Search...', profileImageUrl }: TopNavProps) {
  return (
    <header className="bg-surface/60 backdrop-blur-xl font-body-md text-body-md w-full sticky top-0 z-40 border-b border-primary/10 shadow-sm flex justify-between items-center px-[24px] py-[8px]">
      <div className="flex items-center gap-[16px]">
        {/* Search Bar */}
        <div className="relative w-64 group">
          <span className="material-symbols-outlined absolute left-3 top-1/2 transform -translate-y-1/2 text-on-surface-variant/50 group-focus-within:text-primary transition-colors text-[20px]">search</span>
          <input
            className="w-full bg-surface-container-low border-none rounded-full py-2 pl-10 pr-4 font-body-sm text-body-sm text-on-surface placeholder:text-on-surface-variant/50 focus:ring-1 focus:ring-primary focus:bg-surface transition-all"
            placeholder={searchPlaceholder}
            type="text"
          />
        </div>
      </div>

      <div className="flex items-center gap-[16px]">
        {/* Notification Bell */}
        <button className="w-10 h-10 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-primary-container/10 transition-colors relative">
          <span className="material-symbols-outlined">notifications</span>
          {/* TODO: Unread badge wired to Notification Service in Phase 8 */}
        </button>
        {/* Settings */}
        <button className="w-10 h-10 rounded-full flex items-center justify-center text-on-surface-variant hover:bg-primary-container/10 transition-colors">
          <span className="material-symbols-outlined">settings</span>
        </button>
        {/* User Avatar */}
        <div className="w-8 h-8 rounded-full overflow-hidden border border-primary/20 ml-2">
          {profileImageUrl ? (
            <img alt="User profile" className="w-full h-full object-cover" src={profileImageUrl} />
          ) : (
            <div className="w-full h-full bg-primary/20 flex items-center justify-center text-primary text-xs font-bold">U</div>
          )}
        </div>
      </div>
    </header>
  );
}
