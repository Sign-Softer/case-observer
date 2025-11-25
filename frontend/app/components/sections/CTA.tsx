export default function CTA() {
  return (
    <section className="py-12 sm:py-16 md:py-20 bg-blue-600">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <h2 className="text-2xl sm:text-3xl md:text-4xl font-bold text-white mb-4 sm:mb-6">
          Ready to Never Miss a Case Update?
        </h2>
        <p className="text-lg sm:text-xl text-blue-100 mb-6 sm:mb-8">
          Join legal professionals who trust Case Observer to keep them informed.
        </p>
        <div className="flex flex-col sm:flex-row gap-3 sm:gap-4 justify-center">
          <a
            href="/register"
            className="px-6 sm:px-8 py-3 sm:py-4 bg-white text-blue-600 rounded-lg hover:bg-gray-100 transition-colors font-semibold text-base sm:text-lg"
          >
            Start Free Trial
          </a>
          <a
            href="/login"
            className="px-6 sm:px-8 py-3 sm:py-4 bg-transparent text-white border-2 border-white rounded-lg hover:bg-white hover:text-blue-600 transition-colors font-semibold text-base sm:text-lg"
          >
            Already have an account?
          </a>
        </div>
      </div>
    </section>
  );
}

