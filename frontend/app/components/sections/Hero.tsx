export default function Hero() {
  return (
    <section className="bg-gradient-to-br from-blue-50 to-indigo-100 py-12 sm:py-16 md:py-20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center">
          <h1 className="text-3xl sm:text-4xl md:text-5xl lg:text-6xl font-bold text-gray-900 mb-4 sm:mb-6">
            Never Miss a Court Case Update
          </h1>
          <p className="text-lg sm:text-xl md:text-2xl text-gray-600 mb-6 sm:mb-8 max-w-3xl mx-auto px-4">
            Automated monitoring system for Romanian court cases. Get instant notifications when your cases change status, new hearings are scheduled, or parties are added.
          </p>
          <div className="flex flex-col sm:flex-row gap-3 sm:gap-4 justify-center px-4">
            <a
              href="/register"
              className="px-6 sm:px-8 py-3 sm:py-4 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold text-base sm:text-lg"
            >
              Start Monitoring Free
            </a>
            <a
              href="#how-it-works"
              className="px-6 sm:px-8 py-3 sm:py-4 bg-white text-blue-600 border-2 border-blue-600 rounded-lg hover:bg-blue-50 transition-colors font-semibold text-base sm:text-lg"
            >
              Learn More
            </a>
          </div>
        </div>
      </div>
    </section>
  );
}

