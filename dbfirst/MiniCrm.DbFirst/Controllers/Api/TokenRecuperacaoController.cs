using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MiniCrm.DbFirst.Data;
using MiniCrm.DbFirst.Models;

namespace MiniCrm.DbFirst.Controllers.Api
{
    [Route("api/[controller]")]
    [ApiController]
    public class TokenRecuperacaoController : ControllerBase
    {
        private readonly MiniCrmContext _context;

        public TokenRecuperacaoController(MiniCrmContext context)
        {
            _context = context;
        }

        // GET: api/TokenRecuperacao
        [HttpGet]
        public async Task<ActionResult<IEnumerable<TokenRecuperacao>>> GetTokenRecuperacao()
        {
            return await _context.TokenRecuperacao.ToListAsync();
        }

        // GET: api/TokenRecuperacao/5
        [HttpGet("{id}")]
        public async Task<ActionResult<TokenRecuperacao>> GetTokenRecuperacao(int id)
        {
            var tokenRecuperacao = await _context.TokenRecuperacao.FindAsync(id);

            if (tokenRecuperacao == null)
            {
                return NotFound();
            }

            return tokenRecuperacao;
        }

        // PUT: api/TokenRecuperacao/5
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPut("{id}")]
        public async Task<IActionResult> PutTokenRecuperacao(int id, TokenRecuperacao tokenRecuperacao)
        {
            if (id != tokenRecuperacao.IdToken)
            {
                return BadRequest();
            }

            _context.Entry(tokenRecuperacao).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!TokenRecuperacaoExists(id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }

            return NoContent();
        }

        // POST: api/TokenRecuperacao
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult<TokenRecuperacao>> PostTokenRecuperacao(TokenRecuperacao tokenRecuperacao)
        {
            _context.TokenRecuperacao.Add(tokenRecuperacao);
            await _context.SaveChangesAsync();

            return CreatedAtAction("GetTokenRecuperacao", new { id = tokenRecuperacao.IdToken }, tokenRecuperacao);
        }

        // DELETE: api/TokenRecuperacao/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteTokenRecuperacao(int id)
        {
            var tokenRecuperacao = await _context.TokenRecuperacao.FindAsync(id);
            if (tokenRecuperacao == null)
            {
                return NotFound();
            }

            _context.TokenRecuperacao.Remove(tokenRecuperacao);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        private bool TokenRecuperacaoExists(int id)
        {
            return _context.TokenRecuperacao.Any(e => e.IdToken == id);
        }
    }
}
