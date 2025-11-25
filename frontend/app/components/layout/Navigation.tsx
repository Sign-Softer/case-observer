export default function Navigation() {
  return (
    <nav className="bg-white border-b border-gray-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <a href="/" className="text-2xl font-bold text-blue-600 hover:text-blue-700 transition-colors">
              Case Observer
            </a>
          </div>
          <div className="flex items-center gap-2 sm:gap-4">
            <a
              href="/login"
              className="text-gray-700 hover:text-gray-900 px-3 sm:px-4 py-2 rounded-md text-sm font-medium transition-colors"
            >
              Login
            </a>
            <a
              href="/register"
              className="bg-blue-600 text-white hover:bg-blue-700 px-4 sm:px-6 py-2 rounded-md text-sm font-semibold transition-colors"
            >
              Get Started
            </a>
          </div>
        </div>
      </div>
    </nav>
  );
}

